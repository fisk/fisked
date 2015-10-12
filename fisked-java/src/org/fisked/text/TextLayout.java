package org.fisked.text;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.Buffer;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.Size;
import org.python.modules._threading._threading;

// TODO: This file needs a lot more logic, like valid and invalid regions of the layout for lazy layouting

public class TextLayout {
	private Buffer _buffer;
	private List<Line> _physicalLines = null;
	private List<Line> _logicalLines = null;
	private Rectangle _rect = null;
	private boolean _needsLayout;
	
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
			if (line._trailingEndline) offset++;
		}

		for (int lineNum = fromY; lineNum < toY; lineNum++) {
			Line line = _logicalLines.get(lineNum);
			callback.putLine(offset, line._value, new Point(0, lineNum - _rect.getOrigin().getY()), line._trailingEndline);
			offset += line._value.length();
			if (line._trailingEndline) offset++;
		}
	}
	
	private Point getPointForCharIndexAtOffset(int charIndex, int yOffset) {
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
		
		if (line >= _rect.getOrigin().getY() && line < _rect.getOrigin().getY() + _rect.getSize().getWidth()) {
			return new Point(column, line - yOffset);
		}
		
		return new Point(0, 0);
	}
	
	public Point getAbsolutePointForCharIndex(int charIndex) {
		return getPointForCharIndexAtOffset(charIndex, 0);
	}

	public Point getRelativePointForCharIndex(int charIndex) {
		return getPointForCharIndexAtOffset(charIndex, _rect.getOrigin().getY());
	}
	
	public int getColumnAtCharIndex(int index) {
		return getRelativePointForCharIndex(index).getX();
	}
	
	public class InvalidLocationException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public int getCharIndexForRelativePoint(Point point) throws InvalidLocationException {
		layoutIfNeeded();
		return getCharIndexForAbsolutePoint(point);
	}

	public int getCharIndexForAbsolutePoint(Point point) throws InvalidLocationException {
		layoutIfNeeded();
		int line = 0;
		
		int i = 0;
		
		String lastLine = null;

		for (Line currentLine : _logicalLines) {
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
		
		return i + lineExcess;
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
}
