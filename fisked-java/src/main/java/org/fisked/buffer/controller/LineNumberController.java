/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.buffer.controller;

import java.util.ArrayList;
import java.util.List;

import org.fisked.settings.Settings;
import org.fisked.text.TextLayout;
import org.fisked.text.TextLayout.InvalidLocationException;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Face;
import org.fisked.util.models.Point;

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
