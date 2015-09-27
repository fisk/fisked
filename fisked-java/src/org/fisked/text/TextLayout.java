package org.fisked.text;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.Buffer;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.Size;

//TODO: This file needs a lot more logic

public class TextLayout {
	private Buffer _buffer;
	private List<Line> _physicalLines = null;
	private List<Line> _logicalLines = null;
	private Rectangle _rect = null;
	
	private static class Line {
		public final String _value;
		public final boolean _trailingEndline;
		public Line(String value, boolean trailingEndline) {
			_trailingEndline = trailingEndline;
			_value = value;
		}
	}

	public TextLayout(Buffer buffer, Size size) {
		_rect = new Rectangle(new Point(0, 0), size);
		_buffer = buffer;
		layoutText();
	}

	public Rectangle getClippingRect() {
		return _rect;
	}
	
	public void setClippingRect(Rectangle rect) {
		_rect = rect;
	}

	public void layoutText() {
		_physicalLines = new ArrayList<>();
		_logicalLines = new ArrayList<>();
		
		final int width = _rect.getSize().getWidth();

		int currentLineIndex = 0;
		StringBuilder currentLogicalLine = new StringBuilder();
		StringBuilder currentPhysicalLine = new StringBuilder();

		for (char character : _buffer.getStringBuilder().toString().toCharArray()) {
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
	
	public Point getAbsolutePointForCharIndex(int charIndex) {
		int line = 0;
		int column = 0;
		
		int i = 0;

		exit:
		for (Line currentLine : _logicalLines) {
			for (column = 0; column < currentLine._value.length(); column++) {
				if (i == charIndex) {
					break;
				}
				i++;
			}
			if (currentLine._trailingEndline) {
				if (i == charIndex) {
					break exit;
				}
				i++;
			}
			if (i == charIndex) {
				break exit;
			}
			line++;
		}
		
		if (column >= _rect.getSize().getWidth()) {
			column = 0;
			line++;
		}
		
		if (line >= _rect.getOrigin().getY() && line < _rect.getOrigin().getY() + _rect.getSize().getWidth()) {
			return new Point(column, line);
		}
		
		return new Point(0, 0);
	}

	public Point getRelativePointForCharIndex(int charIndex) {
		return getAbsolutePointForCharIndex(charIndex); // TODO: make it relative instead
	}

	public String getLogicalString() {
		layoutText();
		int fromY = _rect.getOrigin().getY();
		int toY = Math.min(fromY + _rect.getSize().getHeight(), _logicalLines.size());
		StringBuilder result = new StringBuilder();

		for (int line = fromY; line < toY; line++) {
			result.append(_logicalLines.get(line)._value);
			result.append("\n");
		}

		return result.toString();
	}
	
	public int getColumnAtCharIndex(int index) {
		return getRelativePointForCharIndex(index).getX();
	}
	
	public class OutOfBoundsException extends Exception {}
	
	// TODO: make this actually relative
	public int getCharIndexForRelativePoint(Point point) throws OutOfBoundsException {
		return getCharIndexForAbsolutePoint(point);
	}

	public int getCharIndexForAbsolutePoint(Point point) throws OutOfBoundsException {
		int line = 0;
		int column = 0;
		
		int i = 0;
		
		String lastLine = null;

		exit:
		for (Line currentLine : _logicalLines) {
			for (column = 0; column < currentLine._value.length(); column++) {
				if (line == point.getY()) {
					lastLine = currentLine._value;
					break;
				}
				i++;
			}
			if (currentLine._trailingEndline) {
				if (line == point.getY()) {
					lastLine = currentLine._value;
					break exit;
				}
				i++;
			}
			if (line == point.getY()) {
				lastLine = currentLine._value;
				break exit;
			}
			line++;
		}
		
		if (lastLine == null) {
			throw new OutOfBoundsException();
		}

		int lineExcess = Math.min(point.getX(), lastLine.length());
		
		return i + lineExcess;
	}
}
