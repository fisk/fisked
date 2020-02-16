package org.fisk.fisked.lsp.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.fisk.fisked.fileindex.ProjectPaths;
import org.fisk.fisked.text.Buffer;
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
                    var clientLauncher = LSPLauncher.createClientLauncher(new LanguageClient() {
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
                    }, _istream, _ostream);
                    var listeningFuture = clientLauncher.startListening();
                    _server = clientLauncher.getRemoteProxy();
                    try {
                        var initParams = new InitializeParams();
                        initParams.setRootUri(new File(projectPath).toURI().toString());
                        var initialized = _server.initialize(initParams).get();
                        _capabilities = initialized.getCapabilities();
                        synchronized (_lock) {
                            _started = true;
                            _lock.notifyAll();
                        }
                    } catch (Exception e) {
                        _log.error("Exception initializing LSP server", e);
                    }
                } catch (Exception e) {
                    _log.error("Error reading process output: ", e);
                    return;
                }
            }
        }.start();
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

    public void organizeImports(BufferContext bufferContext) {
        try {
            _log.info("Organize imports");
            var context = new CodeActionContext();
            var kinds = new ArrayList<String>();
            kinds.add("source.organizeImports");
            context.setOnly(kinds);
            var lineCount = bufferContext.getTextLayout().getPhysicalLineCount();
            var line = bufferContext.getTextLayout().getLastPhysicalLine();
            var range = new Range(new Position(0, 0), new Position(lineCount - 1, line.getGlyphs().size()));
            var params = new CodeActionParams(bufferContext.getBuffer().getTextDocumentID(), range, context);
            _log.info("Code action: " + params);
            for (var command: _server.getTextDocumentService().codeAction(params).get()) {
                if (command.isLeft()) {
                    var left = command.getLeft();
                } else if (command.isRight()) {
                    var right = command.getRight();
                    var changes = right.getEdit().getChanges();
                _log.info("Got changes: " + changes);
                }
            }
        } catch (Exception e) {
            _log.error("Error organizing imports: ", e);
        }
    }
}
