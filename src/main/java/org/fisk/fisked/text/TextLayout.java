package org.fisk.fisked.text;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.fisk.fisked.ui.Point;
import org.fisk.fisked.ui.Rect;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class TextLayout {
    private static final Logger _log = LogFactory.createLog();

    public static class Glyph {
        private int _x;
        private int _y;
        private int _position;
        private String _character;

        public Glyph(int x, int y, int position, String character) {
            _x = x;
            _y = y;
            _position = position;
            _character = character;
        }

        public int getX() {
            return _x;
        }

        public int getY() {
            return _y;
        }

        public int getPosition() {
            return _position;
        }

        public String getCharacter() {
            return _character;
        }
    }

    public static class Line {
        private int _y;
        private int _startPosition;
        private boolean _isNewline;
        private Line _prev;
        private Line _next;
        private List<Glyph> _glyphs = new ArrayList<>();

        public List<Glyph> getGlyphs() {
            return _glyphs;
        }

        private Line(int y, int startPosition, Line prev, boolean isNewline) {
            _y = y;
            _startPosition = startPosition;
            _prev = prev;
            _isNewline = isNewline;
        }

        private void setNext(Line line) {
            _next = line;
        }

        public boolean isNewline() {
            return _isNewline;
        }

        public int getY() {
            return _y;
        }

        public Line getPrev() {
            return _prev;
        }

        public Line getNext() {
            return _next;
        }

        public int getIndex(int position) {
            return position - _startPosition;
        }

        public Glyph getGlyphAt(int index) {
            if (index < 0 || index >= _glyphs.size()) {
                return null;
            }
            return _glyphs.get(index);
        }

        public Glyph getLastGlyph() {
            if (_glyphs.size() == 0) {
                return null;
            }
            return _glyphs.get(_glyphs.size() - 1);
        }

        public String getCharacterAt(int index) {
            if (index < 0 || index >= _glyphs.size()) {
                return null;
            }
            return _glyphs.get(index).getCharacter();
        }

        public int getStartPosition() {
            return _startPosition;
        }

        public int getEndPosition() {
            var glyph = getLastGlyph();
            if (glyph == null) {
                return getStartPosition();
            } else {
                return glyph.getPosition() + 1;
            }
        }
    }

    private TreeMap<Integer, Line> _logicalLines;
    private TreeMap<Integer, Line> _logicalLineAtPosition;
    private TreeMap<Integer, Line> _physicalLines;
    private TreeMap<Integer, Line> _physicalLineAtPosition;
    private BufferContext _bufferContext;

    public TextLayout(BufferContext bufferContext) {
        _bufferContext = bufferContext;
        calculate();
    }

    public Line getLogicalLineAt(int position) {
        if (position < 0) {
            position = 0;
        }
        return _logicalLineAtPosition.floorEntry(position).getValue();
    }

    public Line getPhysicalLineAt(int position) {
        if (position < 0) {
            position = 0;
        }
        return _physicalLineAtPosition.floorEntry(position).getValue();
    }

    public Line getLastPhysicalLine() {
        return _physicalLines.lastEntry().getValue();
    }

    private static class LayoutIterator {
        Line _line;
        TreeMap<Integer, Line> _lines = new TreeMap<>();
        TreeMap<Integer, Line> _lineAtPosition = new TreeMap<>();
        int _x = 0;
        int _y = -1;
        int _position = 0;
        String _text;
        String _character;
        boolean _isNewline;
        boolean _isWrapped;
        int _width;

        LayoutIterator(String text, int width) {
            _text = text;
            _width = width;
            newLine();
        }

        void newLine() {
            ++_y;
            _x = 0;
            int position = _position;
            if (_isNewline) {
                position++;
            }
            var line  = new Line(_y, position, _line, _isNewline);
            if (_line != null) {
                _line.setNext(line);
            }
            _line = line;
            _lines.put(_y, line);
            _lineAtPosition.put(position, line);
        }

        void insertGlyph() {
            _line.getGlyphs().add(new Glyph(_x, _y, _position, _character));
        }

        void next() {
            ++_position;
        }

        void incX() {
            ++_x;
        }

        boolean hasNext() {
            if (_position < _text.length()) {
                _character = _text.substring(_position, _position + 1);
                _isNewline = _character.equals("\n");
                _isWrapped = _x == _width;
                return true;
            } else {
                return false;
            }
        }

        boolean isNewline() {
            return _isNewline;
        }

        boolean isWrapped() {
            return _isWrapped;
        }

        TreeMap<Integer, Line> getLines() {
            return _lines;
        }

        TreeMap<Integer, Line> getLineAtPosition() {
            return _lineAtPosition;
        }
    }

    private void calculateLogicalLines() {
        int width = _bufferContext.getBufferView().getBounds().getSize().getWidth();
        var string = _bufferContext.getBuffer().getString();
        var iter = new LayoutIterator(string, width);
        while (iter.hasNext()) {
            if (iter.isNewline()) {
                iter.newLine();
            } else if (iter.isWrapped()) {
                iter.newLine();
                iter.insertGlyph();
            } else {
                iter.insertGlyph();
                iter.incX();
            }
            iter.next();
        }
        _logicalLines = iter.getLines();
        _logicalLineAtPosition = iter.getLineAtPosition();
    }

    private void calculatePhysicalLines() {
        int width = _bufferContext.getBufferView().getBounds().getSize().getWidth();
        var string = _bufferContext.getBuffer().getString();
        var iter = new LayoutIterator(string, width);
        while (iter.hasNext()) {
            if (iter.isNewline()) {
                iter.insertGlyph();
                iter.newLine();
            } else {
                iter.insertGlyph();
                iter.incX();
            }
            iter.next();
        }
        _physicalLines = iter.getLines();
        _physicalLineAtPosition = iter.getLineAtPosition();
    }

    public void calculate() {
        calculateLogicalLines();
        calculatePhysicalLines();
        _bufferContext.getBufferView().setNeedsRedraw();
    }

    public Stream<Glyph> getGlyphs() {
        var bufferView = _bufferContext.getBufferView();
        var rect = bufferView.getBounds();
        var start = bufferView.getStartLine();
        var range = _logicalLines.subMap(start, start + rect.getSize().getHeight());
        return range.entrySet().stream().map((entry) -> entry.getValue().getGlyphs()).flatMap((list) -> list.stream());
    }

    public int getLogicalLineCount() {
        return _logicalLines.size();
    }

    public int getPhysicalLineCount() {
        return _physicalLines.size();
    }
}
