package org.fisked.renderingengine.service.models;

public class Point {
	private final int _x;
	private final int _y;
	
	public Point(int x, int y) {
		_x = x;
		_y = y;
	}
	
	public int getX() {
		return _x;
	}
	
	public int getY() {
		return _y;
	}
	
	public String toString() {
		return "{" + _x + ", " + _y + "}";
	}

	public Point addedBy(Point origin) {
		return new Point(_x + origin._x, _y + origin._y);
	}
}
