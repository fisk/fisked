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

import com.google.gson.Gson;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.fisk.fisked.fileindex.ProjectPaths;
import org.fisk.fisked.text.BufferContext;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class JavaLSPClient extends Thread {
    private static final Logger _log = LogFactory.createLog();

    private Object _lock = new Object();
    private boolean _started = false;

    private InputStream _istream;
    private OutputStream _ostream;
    private LanguageServer _server;
    private ServerCapabilities _capabilities;

    private void setup() throws IOException {
        var homePath = System.getProperty("user.home");
        var fiskedHomePath = homePath + "/.fisked";
        var eclipsePath = fiskedHomePath + "/deps/eclipse.jdt.ls/org.eclipse.jdt.ls.product/target/repository";
        var projectPath = ProjectPaths.getProjectRootPath().toString();
        var workspacePath = fiskedHomePath + "/workspace";

        _log.info("LSP eclipse path: " + eclipsePath);
        _log.info("LSP workspace path: " + projectPath);
        _log.info("LSP workspace folder path: " + workspacePath);

        var java = "java";
        var javaArgs = "-Declipse.application=org.eclipse.jdt.ls.core.id1 -Dosgi.bundles.defaultStartLevel=4 -Declipse.product=org.eclipse.jdt.ls.core.product -Dlog.level=ALL";
        var jvmArgs = "-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseZGC --add-modules=ALL-SYSTEM --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED";
        var jarPath = eclipsePath + "/plugins/org.eclipse.equinox.launcher_1.5.700.v20200107-1357.jar";
        var appArgs = "-configuration " + eclipsePath + "/config_linux -data " + workspacePath;
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
                    };
                    var clientLauncher = LSPLauncher.createClientLauncher(client, _istream, _ostream);
                    var listeningFuture = clientLauncher.startListening();
                    _server = clientLauncher.getRemoteProxy();
                    try {
                        var initParams = new InitializeParams();
                        initParams.setRootUri(new File(projectPath).toURI().toString());
                        initParams.setCapabilities(getClientCapabilities());
                        var initialized = _server.initialize(initParams).get();
                        _capabilities = initialized.getCapabilities();
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
        var textDocument = new TextDocumentClientCapabilities();

        var codeActions = new CodeActionCapabilities(false /* dynamic_registration */);
        textDocument.setCodeAction(codeActions);

        var codeAction = new CodeActionCapabilities(false);
        textDocument.setCodeAction(codeAction);

        var clientCapabilities = new ClientCapabilities(workspace, textDocument, null);
        return clientCapabilities;
    }

    public void run() {
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
        var params = new DidOpenTextDocumentParams(textDocument);
        _server.getTextDocumentService().didOpen(params);
    }

    private Diagnostic getDiagnostic(String code, Range range) {
        var diagnostic = new Diagnostic();
        diagnostic.setCode(code);
        diagnostic.setRange(range);
        diagnostic.setSeverity(DiagnosticSeverity.Error);
        diagnostic.setMessage("Test Diagnostic");
        return diagnostic;
    }

    private List<Either<Command, CodeAction>> getCodeActions(BufferContext bufferContext) {
        try {
            _log.info("Get code actions");
            var lineCount = bufferContext.getTextLayout().getPhysicalLineCount();
            var line = bufferContext.getTextLayout().getLastPhysicalLine();
            var range = new Range(new Position(0, 0), new Position(lineCount - 1, line.getGlyphs().size()));
            var diagnostics = new ArrayList<Diagnostic>();
            diagnostics.add(getDiagnostic("source.organizeImports", range));
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
