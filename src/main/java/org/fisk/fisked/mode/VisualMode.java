package org.fisk.fisked.mode;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.ui.Cursor;
import org.fisk.fisked.ui.Point;
import org.fisk.fisked.ui.Rect;
import org.fisk.fisked.ui.Window;

public class VisualMode extends Mode {
    private Window _window;
    private Cursor _other;

    public VisualMode(Window window) {
        super("VISUAL");
        _window = window;
        setupBasicResponders();
    }

    private void setupBasicResponders() {
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
            var minCursor = cursor.getPosition() < _other.getPosition() ? cursor : _other;
            var maxCursor = cursor.getPosition() >= _other.getPosition() ? cursor : _other;
            buffer.remove(minCursor.getPosition(), maxCursor.getPosition());
            window.switchToMode(window.getNormalMode());
        });
        _rootResponder.addEventResponder("c", () -> {
            var minCursor = cursor.getPosition() < _other.getPosition() ? cursor : _other;
            var maxCursor = cursor.getPosition() >= _other.getPosition() ? cursor : _other;
            buffer.remove(minCursor.getPosition(), maxCursor.getPosition());
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("<CTRL>-y", () -> { bufferContext.getBufferView().scrollUp(); });
        _rootResponder.addEventResponder("<CTRL>-e", () -> { bufferContext.getBufferView().scrollDown(); });
        _rootResponder.addEventResponder("$", () -> { cursor.goEndOfLine(); });
        _rootResponder.addEventResponder("^", () -> { cursor.goStartOfLine(); });
        _rootResponder.addEventResponder("h", () -> { cursor.goLeft(); });
        _rootResponder.addEventResponder("l", () -> { cursor.goRight(); });
        _rootResponder.addEventResponder("j", () -> { cursor.goDown(); });
        _rootResponder.addEventResponder("k", () -> { cursor.goUp(); });
        _rootResponder.addEventResponder("<LEFT>", () -> { cursor.goLeft(); });
        _rootResponder.addEventResponder("<RIGHT>", () -> { cursor.goRight(); });
        _rootResponder.addEventResponder("<DOWN>", () -> { cursor.goDown(); });
        _rootResponder.addEventResponder("<UP>", () -> { cursor.goUp(); });
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
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        var minCursor = cursor.getPosition() < _other.getPosition() ? cursor : _other;
        var maxCursor = cursor.getPosition() >= _other.getPosition() ? cursor : _other;
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
