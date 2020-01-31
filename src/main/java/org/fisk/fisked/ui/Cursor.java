package org.fisk.fisked.ui;

import org.fisk.fisked.text.BufferContext;

public class Cursor {
    private int _x;
    private int _y;
    private int _position;
    private int _lastX;
    private boolean _after;

    private BufferContext _bufferContext;

    private void calculate() {
        var point = getPoint();
        _x = point.getX();
        _y = point.getY();
        _bufferContext.getBufferView().setNeedsRedraw();
    }

    public Cursor(BufferContext bufferContext) {
        _bufferContext = bufferContext;
    }

    private Point getPoint() {
        var textLayout = _bufferContext.getTextLayout();
        var line = textLayout.getLogicalLineAt(_position);
        var index = line.getIndex(_position);
        return Point.create(index, line.getY());
    }

    public int getUpPosition(int position, int lastX) {
        return 0;
    }

    public int getX() {
        return _x;
    }

    public int getYRelative() {
        return _y - _bufferContext.getBufferView().getStartLine();
    }

    public int getYAbsolute() {
        return _y;
    }

    public int getPosition() {
        return _position;
    }

    public void goBack() {
        if (_position > 0) {
            --_position;
        }
        calculate();
        _lastX = _x;
    }

    public void goForward() {
        var buffer = Window.getInstance().getBufferContext().getBuffer();
        if (_position < buffer.getLength()) {
            ++_position;
        }
        calculate();
        _lastX = _x;
    }

    public void goLeft() {
        var buffer = Window.getInstance().getBufferContext().getBuffer();
        if (_position > 0) {
            String characterBefore = buffer.getCharacter(_position - 1);
            --_position;
            if (characterBefore.equals("\n")) {
                ++_position;
            }
        }
        calculate();
        _lastX = _x;
    }

    public void goRight() {
        var buffer = Window.getInstance().getBufferContext().getBuffer();
        if (_position < buffer.getLength()) {
            String characterBefore = buffer.getCharacter(_position);
            ++_position;
            if (characterBefore.equals("\n")) {
                --_position;
            }
        }
        calculate();
        _lastX = _x;
    }

    public void goUp() {
        var textLayout = _bufferContext.getTextLayout();
        var position = _position;
        var line = textLayout.getLogicalLineAt(position);
        var prevLine = line.getPrev();
        if (prevLine == null) {
            return;
        }
        var glyph = prevLine.getGlyphAt(_lastX);
        if (glyph == null) {
            glyph = prevLine.getLastGlyph();
            if (glyph != null) {
                _position = glyph.getPosition() + 1;
            } else {
                _position = prevLine.getStartPosition();
            }
        } else {
            _position = glyph.getPosition();
        }
        calculate();
    }

    public void goDown() {
      var textLayout = _bufferContext.getTextLayout();
      var position = _position;
      var line = textLayout.getLogicalLineAt(position);
      var nextLine = line.getNext();
      if (nextLine == null) {
          return;
      }
      var glyph = nextLine.getGlyphAt(_lastX);
      if (glyph == null) {
          glyph = nextLine.getLastGlyph();
          if (glyph != null) {
              _position = glyph.getPosition() + 1;
          } else {
              _position = nextLine.getStartPosition();
          }
      } else {
          _position = glyph.getPosition();
      }
      calculate();
    }

    public void goEndOfLine() {
        var textLayout = _bufferContext.getTextLayout();
        var position = _position;
        var line = textLayout.getPhysicalLineAt(position);
        var glyph = line.getLastGlyph();
        if (glyph == null) {
            _position = line.getStartPosition();
        } else {
            _position = glyph.getPosition() + 1;
        }
        calculate();
    }

    public void goStartOfLine() {
        var textLayout = _bufferContext.getTextLayout();
        var position = _position;
        var line = textLayout.getPhysicalLineAt(position);
        _position = line.getStartPosition();
        calculate();
    }

    public void setPosition(int position) {
        _position = position;
        calculate();
    }

    public void setAfter(boolean after) {
        _after = after;
        calculate();
    }
}
