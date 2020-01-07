package org.fisk.fisked.ui;

import java.io.IOException;
import java.nio.file.Path;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import org.fisk.fisked.EventThread;
import org.fisk.fisked.event.EventListener;
import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.RunnableEvent;
import org.fisk.fisked.event.TextEventResponder;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.Buffer;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class Window implements Drawable {
    private static Logger _log = LogFactory.createLog();
    private static Window _instance;

    public static Window getInstance() {
        return _instance;
    }

    public static void createInstance(Path path) {
        _instance = new Window(path);
    }

    private View _rootView;
    private View _modeLineView;
    private Size _size;
    private BufferView _bufferView;
    private Buffer _buffer;

    private void setupViews() {
        var terminalContext = TerminalContext.getInstance();
        var terminalSize = terminalContext.getScreen().getTerminalSize();
        terminalContext.getTerminal().addResizeListener(new TerminalResizeListener() {
            @Override
            public void onResized(Terminal terminal, TerminalSize newSize) {
                EventThread.getInstance().enqueue(new RunnableEvent(() -> {
                    _log.info("Resize detected");
                    Window.this.update(true /* forced */);
                }));
            }
        });

        _log.info("Terminal size: " + terminalSize.getColumns() + ", " + terminalSize.getRows());
        _rootView = new View(Rect.create(0, 0, terminalSize.getColumns(), terminalSize.getRows()));
        _rootView.setBackgroundColour(TextColor.ANSI.DEFAULT);

        _modeLineView = new ModeLineView(Rect.create(0, terminalSize.getRows() - 1, terminalSize.getColumns(), 1));
        _modeLineView.setResizeMask(View.RESIZE_MASK_BOTTOM | View.RESIZE_MASK_LEFT | View.RESIZE_MASK_RIGHT | View.RESIZE_MASK_HEIGHT);
        _rootView.addSubview(_modeLineView);

        _bufferView = new BufferView(Rect.create(0, 0, terminalSize.getRows() - 1, terminalSize.getColumns()), _buffer);
        _rootView.addSubview(_bufferView);

        _size = _rootView.getBounds().getSize();
    }

    private void setupBindings() {
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

    public Window(Path path) {
        _buffer = new Buffer(path);
        setupViews();
        setupBindings();
    }

    public void setRootView(View view) {
        _rootView = view;
        _size = view.getBounds().getSize();
    }

    public void update(boolean forced) {
        _log.info("Maybe relayout");
        if (!forced && !_rootView.needsRedraw()) {
            _log.info("Relayout not needed");
            return;
        }
        var screen = TerminalContext.getInstance().getScreen();
        var terminalSize = screen.doResizeIfNecessary();
        if (terminalSize == null) {
            terminalSize = new TerminalSize(_rootView.getBounds().getSize().getWidth(),
                                            _rootView.getBounds().getSize().getHeight());
        }
        _log.info("Terminal size: " + terminalSize.getColumns() + ", " + terminalSize.getRows());
        var size = Size.create(terminalSize.getColumns(), terminalSize.getRows());
        if (_size != null && !_size.equals(size)) {
            _log.info("Relayout");
            _rootView.resize(size);
        } else {
            _log.info("Relayout not needed due to same size");
        }
        _rootView.update(Rect.create(0, 0, terminalSize.getColumns(), terminalSize.getRows()), forced);
        _size = size;
        try {
            screen.refresh();
        } catch (IOException e) {}
    }

    @Override
    public void draw(Rect rect) {
        _rootView.draw(rect);
    }
}
