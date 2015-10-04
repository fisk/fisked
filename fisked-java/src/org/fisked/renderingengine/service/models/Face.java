package org.fisked.renderingengine.service.models;

public class Face {
	private final Color _backgroundColor;
	private final Color _foregroundColor;
	private final boolean _bold;
	
	public Face(Color backgroundColor, Color foregroundColor, boolean bold) {
		_backgroundColor = backgroundColor;
		_foregroundColor = foregroundColor;
		_bold = bold;
	}
	
	public Face(Color backgroundColor, Color foregroundColor) {
		this(backgroundColor, foregroundColor, false);
	}
	
	public boolean getBold() {
		return _bold;
	}
	
	public Color getBackgroundColor() {
		return _backgroundColor;
	}
	
	public Color getForegroundColor() {
		return _foregroundColor;
	}
}
