package org.fisked.buffer;

import java.util.ArrayList;
import java.util.List;

import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.settings.Settings;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

public class LineNumberController {
	private BufferWindow _window;
	private int _numberOfDigitsForLineNumbers;
	
	public LineNumberController(BufferWindow window) {
		_window = window;
		_numberOfDigitsForLineNumbers = Settings.getInstance().getNumberOfDigitsForLineNumbers();
	}
	
	private AttributedString drawEmptyRow(Face face) {
		if (_numberOfDigitsForLineNumbers == 0) { return new AttributedString("", face); }
		return new AttributedString(String.format("%" +_numberOfDigitsForLineNumbers +"s", " "), face);
	}
	
	private AttributedString drawNumber(int number, Face face) {
		if (_numberOfDigitsForLineNumbers == 0) { return new AttributedString("", face); }
		String str = String.format("%" +_numberOfDigitsForLineNumbers +"d", number);
		if (str.length() > _numberOfDigitsForLineNumbers) {
			str = "?";
		}
		return new AttributedString(str, face);
	}

	// TODO: Allow themes to extend this
	public List<AttributedString> getLineNumberText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face lineNumberFace = new Face(theme.getLineNumberBackgroundColor(), theme.getLineNumberForegroundColor());

		int height = _window.getTextLayout().getClippingRect().getSize().getHeight();
		int startingLine = _window.getTextLayout().getClippingRect().getOrigin().getY();
		List<Boolean> lineHasTrailingEndline = _window.getTextLayout()
												.getTrailingEndlineInfoForRange(
														new Range(startingLine, startingLine + height));
		
		List<AttributedString> result = new ArrayList<>();
		Boolean nextLineIsNew = true;
		for (int i = 0; i < height; i++) {
			if (nextLineIsNew) {
				result.add(drawNumber(startingLine++, lineNumberFace));
				nextLineIsNew = false;
			} else {
				result.add(drawEmptyRow(lineNumberFace));
			}
			if (i < lineHasTrailingEndline.size() && lineHasTrailingEndline.get(i)) { nextLineIsNew = true; }
		}

		return result;
	}
}