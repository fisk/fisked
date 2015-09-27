package org.fisked.renderingengine.service.models;

public class Size {
	private final int _width;
	private final int _height;
	
	public Size(int width, int height) {
		_width = width;
		_height = height;
	}
	
	public int getWidth() {
		return _width;
	}
	
	public int getHeight() {
		return _height;
	}
}
