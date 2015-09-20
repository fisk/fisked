package org.fisked.theme;

import org.fisked.renderingengine.service.models.Color;

public class FiskedDefaultTheme implements ITheme {

	@Override
	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	@Override
	public Color getForegroundColor() {
		return Color.GREEN;
	}

	@Override
	public Color getCommandForegroundColor() {
		return Color.RED;
	}

}
