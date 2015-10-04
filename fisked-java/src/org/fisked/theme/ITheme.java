package org.fisked.theme;

import org.fisked.renderingengine.service.models.Color;

public interface ITheme {
	Color getSelectionColor();
	Color getBackgroundColor();
	Color getForegroundColor();
	Color getCommandForegroundColor();
}
