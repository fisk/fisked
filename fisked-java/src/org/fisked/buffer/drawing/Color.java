package org.fisked.buffer.drawing;

import jcurses.system.CharColor;

public class Color {
	private final short _color;
	
	public static final Color BLACK =   new Color((short) 0);
	public static final Color RED =     new Color((short) 1);
	public static final Color GREEN =   new Color((short) 2);
	public static final Color YELLOW =  new Color((short) 3);
	public static final Color BLUE =    new Color((short) 4);
	public static final Color MAGENTA = new Color((short) 5);
	public static final Color CYAN = 	new Color((short) 6);
	public static final Color WHITE = 	new Color((short) 7);
	
	public Color(short color) {
		_color = color;
	}
	
	public short getRawColor() {
		return _color;
	}
	
	public CharColor getCharColor() {
		return new CharColor(_color, _color);
	}
	
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Color)) return false;
		Color color = (Color)object;
		return color._color == _color;
	}
}
