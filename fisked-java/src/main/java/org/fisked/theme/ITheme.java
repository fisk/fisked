package org.fisked.theme;

import org.fisked.renderingengine.service.models.Color;

public interface ITheme {
	Color getSelectionBackgroundColor();
	Color getSelectionForegroundColor();
	Color getModelineBackgroundColorDark();
	Color getModelineBackgroundColorLight();
	Color getModelineForegroundColor();
	Color getLineNumberBackgroundColor();
	Color getLineNumberForegroundColor();
	Color getBackgroundColor();
	Color getForegroundColor();
	Color getCommandForegroundColor();
}
