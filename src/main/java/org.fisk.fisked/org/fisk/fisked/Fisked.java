package org.fisk.fisked;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import org.fisk.fisked.event.IOThread;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.ui.Window;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class Fisked {
    private static final Logger _log = LogFactory.createLog();

    private static void setupLogging() {
        try {
            File file = new File("/tmp/fisked.log");
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            System.setErr(ps);
        } catch (Throwable e) {
        }
    }

    private static void setupWindow(Path path) {
        Window.createInstance(path);
        var window = Window.getInstance();
        window.update(true /* forced */);
    }

    private static Path checkArguments(String[] args) {
        if (args.length != 2) {
            System.out.println("fisked: Wrong number of arguments.");
            System.out.println("Try: fisked <file_path>");
            return null;
        }

        try {
            var path = Path.of(args[1]);
            var file = path.toFile();
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        return path;
                    }
                } catch (Exception e) {
                }
                System.out.println("fisked: No such file: " + path.toString());
                return null;
            } else {
                return path;
            }
        } catch (Throwable e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            setupLogging();
            var path = checkArguments(args);
            if (path == null) {
                return;
            }
            _log.info("Fisked started");
            setupWindow(path);
            var eventThread = EventThread.getInstance();
            eventThread.addOnEvent(() -> {
                Window.getInstance().update(false /* forced */);
            });
            eventThread.start();
            new IOThread(TerminalContext.getInstance().getScreen()).start();
        } catch (Exception e) {
            _log.error("Error starting: ", e);
        }
    }
}
