package org.fisk.fisked.lsp.java;

import java.io.InputStream;
import java.io.OutputStream;

public class JavaLSPClient {
    private boolean _started = false;

    private InputStream _istream;
    private OutputStream _ostream;

    public void start() {
        if (_started) {
            return;
        }
        var eclipsePath = "/home/erik/Programming/fisked/deps/eclipse.jdt.ls/org.eclipse.jdt.ls.product/target/repository";
        try {
            var command = "java -Declipse.application=org.eclipse.jdt.ls.core.id1 -Dosgi.bundles.defaultStartLevel=4 -Declipse.product=org.eclipse.jdt.ls.core.product -Dlog.level=ALL -noverify -Xmx1G -jar " + eclipsePath + "/plugins/org.eclipse.equinox.launcher_1.5.700.v20200107-1357.jar -configuration " + eclipsePath + "/config_linux -data ~/Desktop/workspace --add-modules=ALL-SYSTEM --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED";
            var commandArg = command.split(" ");
            var processBuilder = new ProcessBuilder(commandArg);
            processBuilder.start();
            _started = true;
        } catch (Throwable e) {
        }
    }

    static JavaLSPClient _instance = new JavaLSPClient();

    public static JavaLSPClient getInstance() {
        return _instance;
    }
}
