package org.fisked.renderingengine.service.models;

public class Point {
	private int _x;
	private int _y;
	
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
	
	public void setX(int x) {
		_x = x;
	}
	
	public void setY(int y) {
		_y = y;
	}
	
	public String toString() {
		return "{" + _x + ", " + _y + "}";
	}
}
