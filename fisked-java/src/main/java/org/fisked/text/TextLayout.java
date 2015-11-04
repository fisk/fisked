package org.fisked.text;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fisked.buffer.Buffer;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.Size;

// TODO: This file needs a lot more logic, like valid and invalid regions of the layout for lazy layouting

public class TextLayout {
	private final Buffer _buffer;
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

	public TextLayout(Buffer buffer, Size size) {
		_rect = new Rectangle(new Point(0, 0), size);
		_buffer = buffer;
		setNeedsLayout();
	}

	public Rectangle getClippingRect() {
		return _rect;
	}

	public void setClippingRect(Rectangle rect) {
		_rect = rect;
	}

	private void layoutIfNeeded() {
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

		for (char character : _buffer.getCharSequence().toString().toCharArray()) {
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

		for (Line currentLine : _logicalLines) {
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

		if (column >= _rect.getSize().getWidth()) {
			column = 0;
			line++;
		}

		return new Point(column, line - yOffset);
	}

	public Point getAbsoluteLogicalPointForCharIndex(int charIndex) {
		return getPointForCharIndexAtOffset(charIndex, 0, _logicalLines);
	}

	public Point getRelativeLogicalPointForCharIndex(int charIndex) {
		Point point = getPointForCharIndexAtOffset(charIndex, _rect.getOrigin().getY(), _logicalLines);
		int line = point.getY();

		if (line >= 0 && line <= _rect.getSize().getHeight())
			return point;

		return null;
	}

	public Point getAbsolutePhysicalPointForCharIndex(int charIndex) {
		return getPointForCharIndexAtOffset(charIndex, 0, _physicalLines);
	}

	public Point getRelativePhysicalPointForCharIndex(int charIndex) {
		return getPointForCharIndexAtOffset(charIndex, _rect.getOrigin().getY(), _physicalLines);
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
		return getCharIndexForAbsolutePoint(point, _logicalLines);
	}

	public int getCharIndexForRelativePhysicalPoint(Point point) throws InvalidLocationException {
		return getCharIndexForAbsolutePhysicalPoint(point);
	}

	public int getCharIndexForAbsolutePhysicalPoint(Point point) throws InvalidLocationException {
		return getCharIndexForAbsolutePoint(point, _physicalLines);
	}

	public int getCharIndexForAbsolutePoint(Point point, List<Line> lines) throws InvalidLocationException {
		layoutIfNeeded();
		int line = 0;

		int i = 0;

		String lastLine = null;

		Logger logger = LogManager.getLogger();

		for (Line currentLine : lines) {
			logger.debug("Got line: " + currentLine._value.length());
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
			Logger logger = LogManager.getLogger();
			logger.debug("Reached end: " + _buffer.getLength());
			return _buffer.getLength();
		}
	}
}
