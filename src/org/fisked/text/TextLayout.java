package org.fisked.text;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.drawing.Point;
import org.fisked.buffer.drawing.Range;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.log.Log;

public class TextLayout {
	private StringBuilder _string;
	private int _width;
	private List<String> _physicalLines = null;
	private List<String> _logicalLines = null;
	
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
				_physicalLines.add(currentPhysicalLine.toString());
				_logicalLines.add(currentLogicalLine.toString());

				currentLogicalLine = new StringBuilder();
				currentPhysicalLine = new StringBuilder();
				currentLineIndex = 0;
			} else {
				currentPhysicalLine.append(character);
				if (currentLineIndex++ < _width) {
					currentLogicalLine.append(character);
				} else {
					currentLineIndex = 0;
					_logicalLines.add(currentLogicalLine.toString());
					currentLogicalLine = new StringBuilder();
					currentLogicalLine.append(character);
				}
			}
		}
		
		_physicalLines.add(currentPhysicalLine.toString());
		_logicalLines.add(currentLogicalLine.toString());
	}
	
	public Point getLogicalPointForCharIndex(int charIndex, Rectangle rect) {
		int line = 0;
		int column = 0;
		
		String currentLine = _logicalLines.get(line);
		
		for (int i = 0; i < charIndex; i++) {
			if (column >= currentLine.length()) {
				line++;
				currentLine = _logicalLines.get(line);
				column = 0;
			} else {
				column++;
			}
		}
		
		if (line >= rect.getOrigin().getY() && line < rect.getOrigin().getY() + rect.getSize().getWidth()) {
			Log.println("Coordinate: " + column + "," + line);
			return new Point(column, line);	
		}
		
		return null;
	}

	public String getLogicalString(Range range) {
		layoutText();
		int fromY = range.getFrom();
		int toY = Math.min(range.getTo(), _logicalLines.size());
		StringBuilder result = new StringBuilder();
		
		for (int line = fromY; line < toY; line++) {
			result.append(_logicalLines.get(line));
			result.append("\n");
		}
		
		return result.toString();
	}
}
