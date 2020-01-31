package org.fisk.fisked.mode;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.ui.Cursor;
import org.fisk.fisked.ui.Rect;
import org.fisk.fisked.ui.Window;

public class VisualLineMode extends VisualMode {
    public VisualLineMode(Window window) {
        super(window);
    }

    private void deleteSelection() {
        var buffer = _window.getBufferContext().getBuffer();
        var minLine = minCursor().getPhysicalLine();
        var maxLine = maxCursor().getPhysicalLine();
        int start = minLine.getStartPosition();
        int end = maxLine.getEndPosition();
        if (maxLine.getNext() == null) {
            start = Math.max(0, start - 1);
        }
        buffer.remove(start, end);
    }

    @Override
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
            deleteSelection();
            window.switchToMode(window.getNormalMode());
        });
        _rootResponder.addEventResponder("c", () -> {
            deleteSelection();
            window.switchToMode(window.getInputMode());
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
        int minY = minCursor.getYRelative();
        int minX = rect.getPoint().getX();
        int maxY = maxCursor.getYRelative();
        int maxX = minX + rect.getSize().getWidth();
        for (int line = minY; line <= maxY; ++line) {
            graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
            graphics.drawRectangle(new TerminalPosition(minX, line), new TerminalSize(maxX - minX, 1), ' ');
        }
    }

    public boolean isSelected(int position) {
        var minPosition = minCursor().getPhysicalLine().getStartPosition();
        var maxPosition = maxCursor().getPhysicalLine().getEndPosition();
        if (maxCursor().getPhysicalLine().getNext() == null) {
            minPosition = Math.max(0, minPosition - 1);
        }
        return position >= minPosition && position < maxPosition;
    }
}
