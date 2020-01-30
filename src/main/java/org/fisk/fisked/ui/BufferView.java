package org.fisk.fisked.ui;

import com.googlecode.lanterna.TextColor;

import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.BufferContext;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class BufferView extends View {
    private BufferContext _bufferContext;
    private static final Logger _log = LogFactory.createLog();
    private int _startLine = 0;

    public int getStartLine() {
        return _startLine;
    }

    public BufferView(Rect rect, BufferContext bufferContext) {
        super(rect);
        _bufferContext = bufferContext;
    }

    public AttributedString getString() {
        return AttributedString.create(_bufferContext.getBuffer().getString(), _backgroundColour, TextColor.ANSI.DEFAULT);
    }

    @Override
    public void draw(Rect rect) {
        super.draw(rect);
        var terminalContext = TerminalContext.getInstance();
        var textGraphics = terminalContext.getGraphics();
        _log.info("Draw buffer view");
        _bufferContext.getTextLayout().getGlyphs().forEach((glyph) -> {
            var c = AttributedString.create(glyph.getCharacter(), _backgroundColour, TextColor.ANSI.DEFAULT);
            var point = Point.create(rect.getPoint().getX() + glyph.getX(), rect.getPoint().getY() + glyph.getY() - _startLine);
            c.drawAt(point, textGraphics);
        });
    }

    @Override
    public Cursor getCursor() {
        return _bufferContext.getBuffer().getCursor();
    }

    public void adaptCursorToView() {
        int cursorY = getCursor().getYAbsolute();
        var height = getBounds().getSize().getHeight();
        if (cursorY >= _startLine + height) {
            getCursor().goUp();
        } else if (cursorY < _startLine) {
            getCursor().goDown();
        }
    }

    public void adaptViewToCursor() {
        int cursorY = getCursor().getYAbsolute();
        var height = getBounds().getSize().getHeight();
        _log.info("Cursor Y" + cursorY  + " height: " + height + " _startLine: " + _startLine);
        if (cursorY >= _startLine + height) {
            _startLine = cursorY - height + 1;
        } else if (cursorY < _startLine) {
            _startLine = cursorY;
        }
    }

    public void scrollUp() {
        var textLayout = _bufferContext.getTextLayout();
        if (_startLine > textLayout.getLogicalLineCount() - 2) {
            return;
        }
        _startLine++;
        adaptCursorToView();
        setNeedsRedraw();
    }

    public void scrollDown() {
        if (_startLine <= 0) {
            return;
        }
        _startLine--;
        adaptCursorToView();
        setNeedsRedraw();
    }
}
