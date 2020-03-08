package org.fisk.fisked;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import org.fisk.fisked.event.IOThread;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.ui.Window;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class App {
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
        if (args.length != 1) {
            System.out.println("fisked: Wrong number of arguments.");
            System.out.println("Try: fisked <file_path>");
            return null;
        }

        try {
            var path = Path.of(args[0]);
            var file = path.toFile();
            if (!file.exists()) {
                file.createNewFile();
            }

            return path;
        } catch (Throwable e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var path = checkArguments(args);
        if (path == null) {
            return;
        }
        setupLogging();
        _log.info("Fisked started");
        setupWindow(path);
        var eventThread = EventThread.getInstance();
        eventThread.addOnEvent(() -> {
            Window.getInstance().update(false /* forced */);
        });
        eventThread.start();
        new IOThread(TerminalContext.getInstance().getScreen()).start();
    }
}
