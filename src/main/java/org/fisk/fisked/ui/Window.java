package org.fisk.fisked.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.googlecode.lanterna.TerminalPosition;
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
import org.fisk.fisked.lsp.java.JavaLSPClient;
import org.fisk.fisked.mode.InputMode;
import org.fisk.fisked.mode.Mode;
import org.fisk.fisked.mode.NormalMode;
import org.fisk.fisked.mode.VisualLineMode;
import org.fisk.fisked.mode.VisualMode;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.BufferContext;
import org.fisk.fisked.ui.ListView.ListItem;
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
    private ModeLineView _modeLineView;
    private CommandView _commandView;
    private Size _size;
    private BufferContext _bufferContext;
    private NormalMode _normalMode;
    private InputMode _inputMode;
    private VisualMode _visualMode;
    private VisualLineMode _visualLineMode;
    private Mode _currentMode;

    private void setupModes() {
        _normalMode = new NormalMode(this);
        _inputMode = new InputMode(this);
        _visualMode = new VisualMode(this);
        _visualLineMode = new VisualLineMode(this);
        _currentMode = _normalMode;
        _bufferContext.getBufferView().setFirstResponder(_currentMode);
    }

    public void setBufferPath(Path path) {
        var bufferView = _bufferContext.getBufferView();
        bufferView.removeFromParent();
        var rect = bufferView.getBounds();
        _bufferContext = new BufferContext(rect, path);
        _rootView.addSubview(_bufferContext.getBufferView());
        _rootView.setFirstResponder(_bufferContext.getBufferView());
        setupModes();
    }

    private void setupViews(Path path) {
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

        _bufferContext = new BufferContext(Rect.create(0, 0, terminalSize.getColumns(), terminalSize.getRows() - 2), path);
        _rootView = new View(Rect.create(0, 0, terminalSize.getColumns(), terminalSize.getRows()));
        _rootView.setBackgroundColour(TextColor.ANSI.DEFAULT);

        _modeLineView = new ModeLineView(Rect.create(0, terminalSize.getRows() - 2, terminalSize.getColumns(), 1));
        _modeLineView.setResizeMask(View.RESIZE_MASK_BOTTOM | View.RESIZE_MASK_LEFT | View.RESIZE_MASK_RIGHT | View.RESIZE_MASK_HEIGHT);
        _rootView.addSubview(_modeLineView);

        _commandView = new CommandView(Rect.create(0, terminalSize.getRows() - 1, terminalSize.getColumns(), 1));
        _commandView.setResizeMask(View.RESIZE_MASK_BOTTOM | View.RESIZE_MASK_LEFT | View.RESIZE_MASK_RIGHT | View.RESIZE_MASK_HEIGHT);
        _rootView.addSubview(_commandView);

        _rootView.addSubview(_bufferContext.getBufferView());
        _rootView.setFirstResponder(_bufferContext.getBufferView());

        _size = _rootView.getBounds().getSize();
    }

    private void setupBindings() {
        var eventThread = EventThread.getInstance();
        var responders = eventThread.getResponder();
        responders.addEventResponder(new EventResponder() {
            @Override
            public Response processEvent(KeyStrokeEvent event) {
                Window.this.getCommandView().setMessage(null);
                return EventListener.Response.NO;
            }

            @Override
            public void respond() {
            }
        });
        responders.addEventResponder(_rootView);
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
        var lsp = JavaLSPClient.getInstance();
        lsp.start();
        lsp.ensureInit();
        setupViews(path);
        setupBindings();
        setupModes();
    }

    public CommandView getCommandView() {
        return _commandView;
    }

    public Mode getCurrentMode() {
        return _currentMode;
    }

    public Mode getNormalMode() {
        return _normalMode;
    }

    public Mode getInputMode() {
        return _inputMode;
    }

    public Mode getVisualMode() {
        return _visualMode;
    }

    public Mode getVisualLineMode() {
        return _visualLineMode;
    }

    public void switchToMode(Mode mode) {
        _currentMode = mode;
        _bufferContext.getBufferView().setFirstResponder(_currentMode);
        _modeLineView.setNeedsRedraw();
        mode.activate();
    }

    public BufferContext getBufferContext() {
        return _bufferContext;
    }

    public ModeLineView getModeLineView() {
        return _modeLineView;
    }

    public void setRootView(View view) {
        _rootView = view;
        _size = view.getBounds().getSize();
    }

    public View getRootView() {
        return _rootView;
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
        var cursor = _rootView.getCursor();
        if (cursor != null) {
            screen.setCursorPosition(new TerminalPosition(cursor.getX(), cursor.getYRelative()));
        }
        try {
            screen.refresh();
        } catch (IOException e) {}
    }

    @Override
    public void draw(Rect rect) {
        _rootView.draw(rect);
    }

    private ListView _listView;

    public boolean isShowingList() {
        return _listView != null;
    }

    public void showList(List<? extends ListItem> list, String title) {
        if (_listView != null) {
            return;
        }
        var bufferView = _bufferContext.getBufferView();
        var rect = bufferView.getBounds();
        int height = rect.getSize().getHeight();
        int newHeight = height * 2 / 3;
        bufferView.setBounds(Rect.create(rect.getPoint().getX(), rect.getPoint().getY(),
                                         rect.getSize().getWidth(), newHeight));
        bufferView.setNeedsRedraw();
        var listView = new ListView(Rect.create(rect.getPoint().getX(), rect.getPoint().getY() + newHeight,
                                                rect.getSize().getWidth(), height - newHeight), list, title);
        _rootView.addSubview(listView);
        _rootView.setFirstResponder(listView);
        _listView = listView;
        _rootView.setNeedsRedraw();
    }

    public void hideList() {
        var bufferView = _bufferContext.getBufferView();
        var rect = bufferView.getBounds();
        int height = rect.getSize().getHeight();
        int newHeight = height + _listView.getBounds().getSize().getHeight();
        bufferView.setBounds(Rect.create(rect.getPoint().getX(), rect.getPoint().getY(),
                                         rect.getSize().getWidth(), newHeight));
        bufferView.setNeedsRedraw();
        _listView.removeFromParent();
        _rootView.setFirstResponder(_bufferContext.getBufferView());
        _rootView.setNeedsRedraw();
        _listView = null;
    }
}
