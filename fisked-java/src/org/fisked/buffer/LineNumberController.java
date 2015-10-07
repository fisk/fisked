package org.fisked.buffer;

import java.util.ArrayList;
import java.util.List;

import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

public class LineNumberController {
	private BufferWindow _window;
	private int _numberOfDigitsForLineNumbers;
	
	public LineNumberController(BufferWindow window, int numberOfDigitsForLineNumbers) {
		_window = window;
		_numberOfDigitsForLineNumbers = numberOfDigitsForLineNumbers;
	}
	
	private AttributedString drawNumber(int number, Face face) {
		return new AttributedString(String.format("%" +_numberOfDigitsForLineNumbers +"d", number), face);
	}

	// TODO: Allow themes to extend this
	// TODO: Base line numbers on physical lines instead of clipping rect?
	public List<AttributedString> getLineNumberText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face lineNumberFace = new Face(theme.getLineNumberBackgroundColor(), theme.getLineNumberForegroundColor());

		int startingLine = _window.getTextLayout().getClippingRect().getOrigin().getY();
		List<AttributedString> result = new ArrayList<>();
		for (int i = startingLine; i < _window.getTextLayout().getClippingRect().getSize().getHeight() + startingLine; i++) {
			result.add(drawNumber(i, lineNumberFace));
		}

		return result;
	}
}
