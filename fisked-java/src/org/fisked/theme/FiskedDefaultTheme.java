package org.fisked.theme;

import org.fisked.renderingengine.service.models.Color;

public class FiskedDefaultTheme implements ITheme {

	@Override
	public Color getBackgroundColor() {
		return Color.NORMAL;
	}

	@Override
	public Color getSelectionBackgroundColor() {
		return Color.BLUE;
	}

	@Override
	public Color getSelectionForegroundColor() {
		return Color.BLACK;
	}

	@Override
	public Color getForegroundColor() {
		return Color.NORMAL;
	}

	@Override
	public Color getCommandForegroundColor() {
		return Color.RED;
	}

}
