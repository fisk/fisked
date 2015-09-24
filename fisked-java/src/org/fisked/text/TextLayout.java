package org.fisked.text;

import java.util.ArrayList;
import java.util.List;

import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;

public class TextLayout {
	private StringBuilder _string;
	private int _width;
	private List<Line> _physicalLines = null;
	private List<Line> _logicalLines = null;
	
	private static class Line {
		public final String _value;
		public final boolean _trailingEndline;
		public Line(String value, boolean trailingEndline) {
			_trailingEndline = trailingEndline;
			_value = value;
		}
	}

	public TextLayout(StringBuilder string, int width) {
		_string = string;
		_width = width;
		layoutText();
	}

	public void setText(StringBuilder string) {
		_string = string;
		layoutText();
	}

	public void layoutText() {
		_physicalLines = new ArrayList<>();
		_logicalLines = new ArrayList<>();

		int currentLineIndex = 0;
		StringBuilder currentLogicalLine = new StringBuilder();
		StringBuilder currentPhysicalLine = new StringBuilder();

		for (char character : _string.toString().toCharArray()) {
			if (character == '\n') {
				_physicalLines.add(new Line(currentPhysicalLine.toString(), true));
				_logicalLines.add(new Line(currentLogicalLine.toString(), true));

				currentLogicalLine = new StringBuilder();
				currentPhysicalLine = new StringBuilder();
				currentLineIndex = 0;
			} else {
				currentPhysicalLine.append(character);
				currentLogicalLine.append(character);
				
				if (++currentLineIndex == _width) {
					_logicalLines.add(new Line(currentLogicalLine.toString(), false));
					currentLineIndex = 0;
					currentLogicalLine = new StringBuilder();
				}
			}
		}

		_physicalLines.add(new Line(currentPhysicalLine.toString(), false));
		_logicalLines.add(new Line(currentLogicalLine.toString(), false));
	}

	public Point getLogicalPointForCharIndex(int charIndex, Rectangle rect) {
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
		
		if (column >= rect.getSize().getWidth()) {
			column = 0;
			line++;
		}
		
		if (line >= rect.getOrigin().getY() && line < rect.getOrigin().getY() + rect.getSize().getWidth()) {
			return new Point(column, line);
		}
		
		return new Point(0, 0);
	}

	public String getLogicalString(Range range) {
		layoutText();
		int fromY = range.getFrom();
		int toY = Math.min(range.getTo(), _logicalLines.size());
		StringBuilder result = new StringBuilder();

		for (int line = fromY; line < toY; line++) {
			result.append(_logicalLines.get(line)._value);
			result.append("\n");
		}

		return result.toString();
	}
}
