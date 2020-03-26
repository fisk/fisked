package org.fisk.fisked.mode;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.ui.Cursor;
import org.fisk.fisked.ui.Rect;
import org.fisk.fisked.ui.Window;
import org.fisk.fisked.copy.Copy;

public class VisualMode extends Mode {
    protected Cursor _other;

    public VisualMode(Window window) {
        super("VISUAL", window);
        setupBasicResponders();
        setupNavigationResponders();
    }

    protected Cursor minCursor() {
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        return cursor.getPosition() < _other.getPosition() ? cursor : _other;
    }

    protected Cursor maxCursor() {
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        return cursor.getPosition() >= _other.getPosition() ? cursor : _other;
    }

    protected void setupBasicResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        _rootResponder.addEventResponder("<ESC>", () -> { window.switchToMode(window.getNormalMode()); });
        _rootResponder.addEventResponder("o", () -> {
            var position = cursor.getPosition();
            cursor.setPosition(_other.getPosition());
            _other.setPosition(position);
            bufferContext.getBufferView().adaptViewToCursor();
        });
        _rootResponder.addEventResponder("d", () -> {
            buffer.remove(minCursor().getPosition(), maxCursor().getPosition() + 1);
            window.switchToMode(window.getNormalMode());
        });
        _rootResponder.addEventResponder("c", () -> {
            buffer.remove(minCursor().getPosition(), maxCursor().getPosition() + 1);
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("y", () -> {
            var text = buffer.getSubstring(minCursor().getPosition(), maxCursor().getPosition() + 1);
            Copy.getInstance().setText(text, false /* isLine */);
            window.switchToMode(window.getNormalMode());
        });
    }

    @Override
    public void activate() {
        _other = new Cursor(_window.getBufferContext());
        _other.setPosition(_window.getBufferContext().getBuffer().getCursor().getPosition());
    }

    @Override
    public void draw(Rect rect) {
        var terminalContext = TerminalContext.getInstance();
        var graphics = terminalContext.getGraphics();
        var minCursor = minCursor();
        var maxCursor = maxCursor();
        if (maxCursor.getPosition() - minCursor.getPosition() == 0) {
            return;
        }
        int minY = minCursor.getYRelative();
        int minX = minCursor.getX();
        int maxY = maxCursor.getYRelative();
        int maxX = maxCursor.getX();
        for (int line = minY; line <= maxY; ++line) {
            int fromColumn = rect.getPoint().getX();
            int toColumn = fromColumn + rect.getSize().getWidth();
            if (line == minY) {
                fromColumn = minX;
            }
            if (line == maxY) {
                toColumn = maxX;
            }
            graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
            graphics.drawRectangle(new TerminalPosition(fromColumn, line), new TerminalSize(toColumn - fromColumn, 1), ' ');
        }
    }

    public boolean isSelected(int position) {
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        var minCursor = cursor.getPosition() < _other.getPosition() ? cursor : _other;
        var maxCursor = cursor.getPosition() >= _other.getPosition() ? cursor : _other;
        return position >= minCursor.getPosition() && position <= maxCursor.getPosition();
    }
}
