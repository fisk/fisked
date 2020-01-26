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
        var position = _position - 1;
        var line = textLayout.getLineAt(position);
        var index = line.getIndex(position);
        var character = _bufferContext.getBuffer().getCharacter(position);
        var isFirst = false;
        if (index < 0) {
            index = 0;
            isFirst = true;
        }
        var point = Point.create(index, line.getY());
        if (!isFirst && _after && !character.equals("\n")) {
            var width = _bufferContext.getBufferView().getBounds().getSize().getWidth();
            if (point.getX() < width - 1) {
                point = Point.create(point.getX() + 1, point.getY());
            } else {
                point = Point.create(0, point.getY() + 1);
            }
        }
        return point;
    }

    public int getUpPosition(int position, int lastX) {
        return 0;
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

    public void goBack() {
        var buffer = Window.getInstance().getBufferContext().getBuffer();
        if (_position > 0) {
            --_position;
        }
        calculate();
    }

    public void goForward() {
        var buffer = Window.getInstance().getBufferContext().getBuffer();
        if (_position < buffer.getLength()) {
            ++_position;
        }
        calculate();
    }

    public void goLeft() {
        var buffer = Window.getInstance().getBufferContext().getBuffer();
        if (_position > 0) {
            String characterBefore = buffer.getCharacter(_position - 1);
            --_position;
            String characterAfter = buffer.getCharacter(_position - 1);
            if (!characterBefore.equals("\n") && characterAfter.equals("\n")) {
                --_position;
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
            String characterAfter = _position == buffer.getLength() ? "" : buffer.getCharacter(_position);
            if (characterBefore.equals("\n") && !characterAfter.equals("\n")) {
                ++_position;
            }
        }
        calculate();
        _lastX = _x;
    }

    public void goUp() {
    }

    public void goDown() {
    }

    public void setAfter(boolean after) {
        _after = after;
        calculate();
    }
}
