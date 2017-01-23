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
package org.fisked.text;

import java.util.ArrayList;
import java.util.List;

import org.fisked.util.models.Point;
import org.fisked.util.models.Range;
import org.fisked.util.models.Rectangle;
import org.fisked.util.models.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: This file needs a lot more logic, like valid and invalid regions of the layout for lazy layouting

public class TextLayout {
	private static final Logger LOG = LoggerFactory.getLogger(TextLayout.class);

	private final CharSequence _text;
	private List<Line> _physicalLines = null;
	private List<Line> _logicalLines = null;
	private Rectangle _rect = null;
	private boolean _needsLayout = true;

	private static class Line {
		public final String _value;
		public final boolean _trailingEndline;

		public Line(String value, boolean trailingEndline) {
			_trailingEndline = trailingEndline;
			_value = value;
		}
	}

	public void setNeedsLayout() {
		_needsLayout = true;
	}

	public TextLayout(CharSequence text, Size size) {
		_rect = new Rectangle(new Point(0, 0), size);
		_text = text;
		setNeedsLayout();
	}

	public Rectangle getClippingRect() {
		return _rect;
	}

	public void setClippingRect(Rectangle rect) {
		_rect = rect;
	}

	public void layoutIfNeeded() {
		if (_needsLayout) {
			layoutText();
			_needsLayout = false;
		}
	}

	private void layoutText() {
		_physicalLines = new ArrayList<>();
		_logicalLines = new ArrayList<>();

		final int width = _rect.getSize().getWidth();

		int currentLineIndex = 0;
		StringBuilder currentLogicalLine = new StringBuilder();
		StringBuilder currentPhysicalLine = new StringBuilder();

		for (char character : _text.toString().toCharArray()) {
			if (character == '\n') {
				_physicalLines.add(new Line(currentPhysicalLine.toString(), true));
				_logicalLines.add(new Line(currentLogicalLine.toString(), true));

				currentLogicalLine = new StringBuilder();
				currentPhysicalLine = new StringBuilder();
				currentLineIndex = 0;
			} else {
				currentPhysicalLine.append(character);
				currentLogicalLine.append(character);

				if (++currentLineIndex == width) {
					_logicalLines.add(new Line(currentLogicalLine.toString(), false));
					currentLineIndex = 0;
					currentLogicalLine = new StringBuilder();
				}
			}
		}

		_physicalLines.add(new Line(currentPhysicalLine.toString(), false));
		_logicalLines.add(new Line(currentLogicalLine.toString(), false));
	}

	public interface GetStringCallback {
		void putLine(int offset, String line, Point relativePoint, boolean physicalLine);
	}

	public void getLogicalString(GetStringCallback callback) {
		layoutIfNeeded();
		int offset = 0;
		int fromY = _rect.getOrigin().getY();
		int toY = Math.min(fromY + _rect.getSize().getHeight(), _logicalLines.size());

		LOG.debug("Logical string from y: " + fromY + ", to y: " + toY);

		for (int lineNum = 0; lineNum < fromY; lineNum++) {
			Line line = _logicalLines.get(lineNum);
			offset += line._value.length();
			if (line._trailingEndline)
				offset++;
		}

		for (int lineNum = fromY; lineNum < toY; lineNum++) {
			Line line = _logicalLines.get(lineNum);
			callback.putLine(offset, line._value, new Point(0, lineNum - _rect.getOrigin().getY()),
					line._trailingEndline);
			offset += line._value.length();
			if (line._trailingEndline)
				offset++;
		}
	}

	private Point getPointForCharIndexAtOffset(int charIndex, int yOffset, List<Line> lines) {
		layoutIfNeeded();
		int line = 0;
		int column = 0;

		int i = 0;

		for (Line currentLine : lines) {
			if (i + currentLine._value.length() >= charIndex) {
				column = charIndex - i;
				break;
			} else {
				i += currentLine._value.length();
			}

			if (currentLine._trailingEndline) {
				i++;
			}
			line++;
		}

		return new Point(column, line - yOffset);
	}

	public Point getAbsoluteLogicalPointForCharIndex(int charIndex) {
		layoutIfNeeded();
		return getPointForCharIndexAtOffset(charIndex, 0, _logicalLines);
	}

	public Point getRelativeLogicalPointForCharIndex(int charIndex) {
		layoutIfNeeded();
		Point point = getPointForCharIndexAtOffset(charIndex, _rect.getOrigin().getY(), _logicalLines);
		int line = point.getY();
		int column = point.getX();

		if (column >= _rect.getSize().getWidth()) {
			column = 0;
			line++;
		}

		if (line >= 0 && line <= _rect.getSize().getHeight())
			return new Point(column, line);

		return null;
	}

	public Point getAbsolutePhysicalPointForCharIndex(int charIndex) {
		layoutIfNeeded();
		return getPointForCharIndexAtOffset(charIndex, 0, _physicalLines);
	}

	public int getColumnAtCharIndex(int index) {
		return getRelativeLogicalPointForCharIndex(index).getX();
	}

	public class InvalidLocationException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public int getCharIndexForRelativeLogicalPoint(Point point) throws InvalidLocationException {
		return getCharIndexForAbsoluteLogicalPoint(point);
	}

	public int getCharIndexForAbsoluteLogicalPoint(Point point) throws InvalidLocationException {
		layoutIfNeeded();
		return getCharIndexForAbsolutePoint(point, _logicalLines);
	}

	public int getCharIndexForAbsolutePhysicalPoint(Point point) throws InvalidLocationException {
		layoutIfNeeded();
		return getCharIndexForAbsolutePoint(point, _physicalLines);
	}

	public int getCharIndexForAbsolutePoint(Point point, List<Line> lines) throws InvalidLocationException {
		layoutIfNeeded();
		int line = 0;

		int i = 0;

		String lastLine = null;

		for (Line currentLine : lines) {
			if (line == point.getY()) {
				lastLine = currentLine._value;
				break;
			}
			i += currentLine._value.length();
			if (currentLine._trailingEndline) {
				i++;
			}
			line++;
		}

		if (lastLine == null) {
			throw new InvalidLocationException();
		}

		int lineExcess = Math.min(point.getX(), lastLine.length());

		int result = i + lineExcess;

		return result;
	}

	public List<Boolean> getTrailingEndlineInfoForRange(Range range) {
		layoutIfNeeded();
		List<Boolean> result = new ArrayList<>();
		if (range.getStart() > _logicalLines.size()) {
			return result;
		}

		int lineIndex = range.getStart();
		while (lineIndex <= range.getEnd() && lineIndex < _logicalLines.size()) {
			result.add(_logicalLines.get(lineIndex++)._trailingEndline);
		}

		return result;
	}

	public int getCharIndexForPhysicalLine(int number) {
		try {
			return getCharIndexForAbsolutePhysicalPoint(new Point(0, number));
		} catch (InvalidLocationException e) {
			LOG.debug("Reached end: " + _text.length());
			return _text.length();
		}
	}
}
