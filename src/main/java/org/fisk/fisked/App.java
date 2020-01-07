package org.fisk.fisked;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import org.fisk.fisked.event.EventListener;
import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.IOThread;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.RunnableEvent;
import org.fisk.fisked.event.TextEventResponder;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.ui.ModeLineView;
import org.fisk.fisked.ui.Rect;
import org.fisk.fisked.ui.View;
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

    private static void setupBindings() {
        var eventThread = EventThread.getInstance();
        var responders = eventThread.getResponder();
        responders.addEventResponder(new TextEventResponder("q", () -> {
            System.exit(0);
        }));
        responders.addEventResponder(new EventResponder() {
            @Override
            public Response processEvent(KeyStrokeEvent event) {
                if (event.getKeyStroke().getKeyType() == KeyType.EOF) {
                    return EventListener.Response.YES;
                }
                return EventListener.Response.NO;
            }
            @Override
            public void respond() {
                System.exit(0);
            }
        });
    }

    private static void setupWindow() {
        var terminalContext = TerminalContext.getInstance();
        var terminalSize = terminalContext.getScreen().getTerminalSize();
        var window = Window.getInstance();

        terminalContext.getTerminal().addResizeListener(new TerminalResizeListener() {
            @Override
            public void onResized(Terminal terminal, TerminalSize newSize) {
                EventThread.getInstance().enqueue(new RunnableEvent(() -> {
                    _log.info("Resize detected");
                    window.update(true /* forced */);
                }));
            }
        });

        _log.info("Terminal size: " + terminalSize.getColumns() + ", " + terminalSize.getRows());
        var rootView = new View(Rect.create(0, 0, terminalSize.getColumns(), terminalSize.getRows()));
        rootView.setBackgroundColour(TextColor.ANSI.DEFAULT);
        var modeLine = new ModeLineView(Rect.create(0, terminalSize.getRows() - 1, terminalSize.getColumns(), 1));
        modeLine.setResizeMask(View.RESIZE_MASK_BOTTOM | View.RESIZE_MASK_LEFT | View.RESIZE_MASK_RIGHT | View.RESIZE_MASK_HEIGHT);
        rootView.addSubview(modeLine);
        window.setRootView(rootView);
        window.update(true /* forced */);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        setupLogging();
        _log.info("Fisked started");
        var terminalContext = TerminalContext.getInstance();
        var screen = terminalContext.getScreen();

        setupBindings();
        setupWindow();
        var eventThread = EventThread.getInstance();
        eventThread.addOnEvent(() -> {
            Window.getInstance().update(false /* forced */);
        });
        eventThread.start();
        new IOThread(screen).start();
    }
}
