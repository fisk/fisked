package org.fisked.buffer;

import java.util.ArrayList;
import java.util.List;

import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.settings.Settings;
import org.fisked.text.TextLayout;
import org.fisked.text.TextLayout.InvalidLocationException;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

public class LineNumberController {
	private final BufferWindow _window;
	private final int _numberOfDigitsForLineNumbers;

	public LineNumberController(BufferWindow window) {
		_window = window;
		_numberOfDigitsForLineNumbers = Settings.getInstance().getNumberOfDigitsForLineNumbers();
	}

	private AttributedString drawEmptyRow(Face face) {
		if (_numberOfDigitsForLineNumbers == 0) {
			return new AttributedString("", face);
		}
		return new AttributedString(String.format("%" + _numberOfDigitsForLineNumbers + "s", " "), face);
	}

	private AttributedString drawNumber(int number, Face face) {
		if (_numberOfDigitsForLineNumbers == 0) {
			return new AttributedString("", face);
		}
		String str = String.format("%" + _numberOfDigitsForLineNumbers + "d", number);
		if (str.length() > _numberOfDigitsForLineNumbers) {
			str = "?";
		}
		return new AttributedString(str, face);
	}

	// TODO: Allow themes to extend this
	public List<AttributedString> getLineNumberText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face lineNumberFace = new Face(theme.getLineNumberBackgroundColor(), theme.getLineNumberForegroundColor());

		TextLayout layout = _window.getTextLayout();
		int height = layout.getClippingRect().getSize().getHeight();
		int offset = layout.getClippingRect().getOrigin().getY();

		List<AttributedString> result = new ArrayList<>();
		int prevLine = -1;
		for (int i = 0; i < height; i++) {
			int firstLineChar;
			try {
				firstLineChar = layout.getCharIndexForRelativeLogicalPoint(new Point(0, i + offset));
			} catch (InvalidLocationException e) {
				firstLineChar = _window.getBuffer().length();
			}
			int currentLine = layout.getAbsolutePhysicalPointForCharIndex(firstLineChar).getY();
			if (prevLine != currentLine) {
				result.add(drawNumber(currentLine, lineNumberFace));
			} else {
				result.add(drawEmptyRow(lineNumberFace));
			}
			prevLine = currentLine;
		}

		return result;
	}
}
