package org.fisked.theme;

import org.fisked.buffer.drawing.Color;

public class FiskedDefaultTheme implements ITheme {

	@Override
	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	@Override
	public Color getForegroundColor() {
		return Color.MAGENTA;
	}

}
