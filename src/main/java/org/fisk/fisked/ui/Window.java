package org.fisk.fisked.ui;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;

import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class Window implements Drawable {
    private static Logger _log = LogFactory.createLog();
    private static volatile Window _instance;

    public static Window getInstance() {
        var instance = _instance;
        if (instance == null) {
            synchronized (Window.class) {
                instance = _instance;
                if (instance == null) {
                    instance = new Window();
                    _instance = instance;
                }
            }
        }
        return instance;
    }

    private View _rootView;
    private Size _size;

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
