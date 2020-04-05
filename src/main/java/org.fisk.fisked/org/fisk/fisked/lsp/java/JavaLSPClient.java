package org.fisk.fisked.lsp.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticHighlightingCapabilities;
import org.eclipse.lsp4j.SemanticHighlightingParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.fisk.fisked.fileindex.ProjectPaths;
import org.fisk.fisked.text.BufferContext;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

import com.google.gson.Gson;

public class JavaLSPClient extends Thread {
    private static final Logger _log = LogFactory.createLog();

    private Object _lock = new Object();
    private boolean _started = false;
    private boolean _enabled = true;

    private InputStream _istream;
    private OutputStream _ostream;
    private LanguageServer _server;
    private ServerCapabilities _capabilities;
    
    private String _homePath = System.getProperty("user.home");
    private String _fiskedHomePath = _homePath + "/.fisked";
    private String _eclipsePath = _fiskedHomePath + "/deps/eclipse.jdt.ls/org.eclipse.jdt.ls.product/target/repository";
    private String _projectPath = ProjectPaths.getProjectRootPath().toString();
    private String _workspacePath = _fiskedHomePath + "/workspace";
    
    public JavaLSPClient() {
        if (!new File(_eclipsePath).exists()) {
            _log.info("No LSP support");
            _enabled = false;
        }
    }

    private void setup() throws IOException {
        _log.info("LSP eclipse path: " + _eclipsePath);
        _log.info("LSP workspace path: " + _projectPath);
        _log.info("LSP workspace folder path: " + _workspacePath);

        var java = "java";
        var javaArgs = "-Declipse.application=org.eclipse.jdt.ls.core.id1 -Dosgi.bundles.defaultStartLevel=4 -Declipse.product=org.eclipse.jdt.ls.core.product -Dlog.level=ALL";
        var jvmArgs = "-Xmx4G --add-modules=ALL-SYSTEM --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED";
        var jarPath = _eclipsePath + "/plugins/org.eclipse.equinox.launcher_1.5.700.v20200107-1357.jar";
        var appArgs = "-configuration " + _eclipsePath + "/config_linux -data " + _workspacePath;
        var command = java + " " + javaArgs +  " " + jvmArgs + " -jar " + jarPath + " " + appArgs;
        var commandArg = command.split(" ");
        var processBuilder = new ProcessBuilder(commandArg);
        var process = processBuilder.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (!_started) {
                    process.destroy();
                } else {
                    try {
                        _server.shutdown().get();
                        _server.exit();
                    } catch (Exception e) {
                        process.destroy();
                    }
                }
            }
        });

        _log.info("Proccess command: " + command);
        _log.info("Process PID: " + process.pid());

        new Thread() {
            public void run() {
                try {
                    _log.info("Starting LSP server...");
                    _istream = process.getInputStream();
                    _ostream = process.getOutputStream();
                    var client = new LanguageClient() {
                        @Override
                        public void telemetryEvent(Object object) {
                            _log.info("telemetryEvent called");
                        }
                        @Override
                        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
                            _log.info("publishDiagnostics called");
                        }
                        @Override
                        public void showMessage(MessageParams message) {
                            _log.info("showMessage: " + message.getMessage());
                        }
                        @Override
                        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
                            _log.info("showMessageRequest called");
                            return null;
                        }
                        @Override
                        public void logMessage(MessageParams message) {
                            _log.info("logMessage: " + message.getMessage());
                        }
                        @Override
                        public void semanticHighlighting(SemanticHighlightingParams params) {
                            _log.info("Semantic info: " + params);
                        }
                        @Override
                        public CompletableFuture<List<WorkspaceFolder>> workspaceFolders() {
                            _log.info("Workspace folders?");
                            return null;
                        }
                        @Override
                        public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
                            _log.info("Configuration?");
                            throw new UnsupportedOperationException();
                        }
                        public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                            _log.info("Workspace edit?");
                            throw new UnsupportedOperationException();
                        }
                        public CompletableFuture<Void> registerCapability(RegistrationParams params) {
                            _log.info("Register capability?");
                            throw new UnsupportedOperationException();
                        }
                        public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
                            _log.info("Unregister capability?");
                            throw new UnsupportedOperationException();
                        }
                    };
                    var clientLauncher = LSPLauncher.createClientLauncher(client, _istream, _ostream);
                    var listeningFuture = clientLauncher.startListening();
                    _server = clientLauncher.getRemoteProxy();
                    try {
                        var initParams = new InitializeParams();
                        initParams.setRootUri(new File(_projectPath).toURI().toString());
                        initParams.setCapabilities(getClientCapabilities());
                        var initialized = _server.initialize(initParams).get();
                        _capabilities = initialized.getCapabilities();
                        _log.info("Server capabilities: " + _capabilities);
                        synchronized (_lock) {
                            _started = true;
                            _lock.notifyAll();
                        }
                    } catch (Exception e) {
                        _log.error("Exception initializing LSP server", e);
                    }
                    listeningFuture.get();
                } catch (Exception e) {
                    _log.error("Error reading process output: ", e);
                    return;
                }
            }
        }.start();
    }

    private ClientCapabilities getClientCapabilities() {
        var workspace = new WorkspaceClientCapabilities();
        workspace.setApplyEdit(true);
        var textDocument = new TextDocumentClientCapabilities();

        var semanticHighlighting = new SemanticHighlightingCapabilities(true);
        textDocument.setSemanticHighlightingCapabilities(semanticHighlighting);

        var codeAction = new CodeActionCapabilities(true);
        textDocument.setCodeAction(codeAction);

        var clientCapabilities = new ClientCapabilities(workspace, textDocument, null);
        return clientCapabilities;
    }

    public void run() {
        if (!_enabled) {
            return;
        }
        try {
            setup();
        } catch (Throwable e) {
            _log.error("Error setting up LSP server", e);
        }
    }

    public LanguageServer getServer() {
        return _server;
    }

    static JavaLSPClient _instance = new JavaLSPClient();

    public static JavaLSPClient getInstance() {
        return _instance;
    }

    public void ensureInit() {
        if (!_enabled) {
            return;
        }
        for (;;) {
            synchronized (_lock) {
                if (_started) {
                    break;
                } else {
                    try {
                        _lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public void postDidOpen(TextDocumentItem textDocument) {
        if (!_enabled) {
            return;
        }
        var params = new DidOpenTextDocumentParams(textDocument);
        _server.getTextDocumentService().didOpen(params);
    }

    public List<ColorInformation> decorateBuffer(BufferContext bufferContext) {
        if (!_enabled) {
            return new ArrayList<ColorInformation>();
        }
        try {
            _log.info("Decorate buffer");
            var colorParams = new DocumentColorParams(bufferContext.getBuffer().getTextDocumentID());
            return _server.getTextDocumentService().documentColor(colorParams).join();
        } catch (Exception e) {
            _log.error("Error getting colours: ", e);
            throw new RuntimeException("Error getting code actions: ", e);
        }
    }

    private List<Either<Command, CodeAction>> getCodeActions(BufferContext bufferContext) {
        try {
            _log.info("Get code actions");
            var lineCount = bufferContext.getTextLayout().getPhysicalLineCount();
            var line = bufferContext.getTextLayout().getLastPhysicalLine();
            var range = new Range(new Position(0, 0), new Position(lineCount - 1, line.getGlyphs().size()));
            var diagnostics = new ArrayList<Diagnostic>();
            var context = new CodeActionContext(diagnostics);
            var params = new CodeActionParams(bufferContext.getBuffer().getTextDocumentID(), range, context);
            _log.info("Code action: " + params);
            return _server.getTextDocumentService().codeAction(params).join();
        } catch (Exception e) {
            _log.error("Error getting code actions: ", e);
            throw new RuntimeException("Error getting code actions: ", e);
        }
    }

    private Command getCodeActionCommand(BufferContext bufferContext, String title) {
        for (var either: getCodeActions(bufferContext)) {
            if (either.isLeft()) {
                _log.info("Code action: " + either);
                var command = either.getLeft();
                if (command.getTitle().equals(title)) {
                    return command;
                }
            }
        }
        return null;
    }

    private void applyWorkspaceEdit(BufferContext context, List<Object> args) {
        var json = args.get(0).toString();
        _log.info("applyWorkspaceEdit: " + json);
        var root = (Map<String, Object>)new Gson().fromJson(json, HashMap.class);
        var changes = (Map<String, Object>)root.get("changes");
        for (var changeEntry: changes.entrySet()) {
            URI uri = null;
            try {
                uri = new URI(changeEntry.getKey());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid URI", e);
            }
            if (!uri.equals(context.getBuffer().getURI())) {
                throw new RuntimeException("Applying workspace edit to unexpected URI: " + uri);
            }
            var edits = (List<Map<String, Object>>) changeEntry.getValue();
            for (var edit: edits) {
                var range = (Map<String, Object>) edit.get("range");
                var startPoint = (Map<String, Double>)range.get("start");
                var startLine = (int) (double)startPoint.get("line");
                var startCharacter = (int) (double)startPoint.get("character");
                var startIndex = context.getTextLayout().getIndexForPhysicalLineCharacter(startLine, startCharacter);
                var endPoint = (Map<String, Double>) range.get("end");
                var endLine = (int) (double)endPoint.get("line");
                var endCharacter = (int) (double)endPoint.get("character");
                var endIndex = context.getTextLayout().getIndexForPhysicalLineCharacter(endLine, endCharacter);
                var buffer = context.getBuffer();
                var newText = (String)edit.get("newText");
                _log.info("Insert " + newText + " at " + startIndex);
                _log.info("Remove [" + startIndex + ", " + endIndex + "]");
                buffer.remove(startIndex, endIndex);
                buffer.insert(startIndex, newText);
            }
        }
    }

    private void applyCommand(BufferContext bufferContext, Command command) {
        switch (command.getCommand()) {
            case "java.apply.workspaceEdit":
                applyWorkspaceEdit(bufferContext, command.getArguments());
                break;
            default:
                throw new RuntimeException("Unknown command: " + command);
        }
    }

    public void organizeImports(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        try {
            var command = getCodeActionCommand(bufferContext, "Organize imports");
            if (command != null) {
                applyCommand(bufferContext, command);
            }
        } catch (Exception e) {
            _log.error("Exception: ", e);
        }
    }
}
