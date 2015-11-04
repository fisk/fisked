package org.fisked.renderingengine.service.models;

public class Rectangle {
	private final Point _origin;
	private final Size _size;
	
	public Point getOrigin() {
		return _origin;
	}
	
	public Size getSize() {
		return _size;
	}
	
	public Rectangle(int x, int y, int width, int height) {
		_origin = new Point(x, y);
		_size = new Size(width, height);
	}
	
	public Rectangle(Point origin, Size size) {
		_origin = origin;
		_size = size;
	}
	
	public String toString() {
		return "[" + _origin.getX() + ", " + _origin.getY() + ", " + _size.getWidth() + ", " + _size.getHeight() + "]";
	}
}
