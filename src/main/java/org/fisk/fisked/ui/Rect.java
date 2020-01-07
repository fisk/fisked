package org.fisk.fisked.ui;

public class Rect {
    private Point _point;
    private Size _size;

    private Rect(Point point, Size size) {
        _point = point;
        _size = size;
    }

    public Point getPoint() {
        return _point;
    }

    public Size getSize() {
        return _size;
    }

    public static Rect create(int x, int y, int width, int height) {
        return new Rect(Point.create(x, y), Size.create(width, height));
    }

    public boolean equals(Rect rect) {
        return _point.equals(rect._point) && _size.equals(rect._size);
    }
}
