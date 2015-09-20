package org.fisked.renderingengine.service.models;

public class Size {
	private int _width;
	private int _height;
	
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
	
	public void setWidth(int width) {
		_width = width;
	}
	
	public void setHeight(int height) {
		_height = height;
	}
}
