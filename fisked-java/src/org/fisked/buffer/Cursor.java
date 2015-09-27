package org.fisked.buffer;

import org.fisked.renderingengine.service.models.Point;
import org.fisked.text.TextLayout;
import org.fisked.text.TextLayout.InvalidLocationException;

public class Cursor {
	private int _charIndex;
	private Point _relativePoint; // relative point is point on screen with view top left being 0, 0
	private Point _absolutePoint; // absolute point is absolute position of cursor from where the text begins
	private int _lastColumn; // last column of any command that changes cursor position side-ways
	private TextLayout _layout;
	
	protected Cursor(int charIndex, Point relativePoint, Point absolutePoint, int lastColumn, TextLayout layout) {
		_charIndex = charIndex;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		_lastColumn = lastColumn;
		_layout = layout;
	}
	
	public Cursor(TextLayout layout) {
		this(0, new Point(0, 0), new Point(0, 0), 0, layout);
	}
	
	public static Cursor makeCursorFromCharIndex(int index, TextLayout layout) {
		Point relative = layout.getRelativePointForCharIndex(index);
		Point absolute = layout.getAbsolutePointForCharIndex(index);
		int column = relative.getX();
		return new Cursor(index, relative, absolute, column, layout);
	}
	
	public int getCharIndex() {
		return _charIndex;
	}
	
	public Point getRelativePoint() {
		return _relativePoint;
	}
	
	public Point getAbsolutePoint() {
		return _absolutePoint;
	}
	
	public int getLastColumn() {
		return _lastColumn;
	}
	
	public void setCharIndex(int index, boolean changeLastColumn) {
		Point relativePoint = _layout.getRelativePointForCharIndex(index);
		Point absolutePoint = _layout.getAbsolutePointForCharIndex(index);
		_charIndex = index;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		if (changeLastColumn) {
			_lastColumn = relativePoint.getX();
		}
	}
	
	public void setRelativePoint(Point relativePoint, boolean changeLastColumn) throws InvalidLocationException {
		int index = _layout.getCharIndexForRelativePoint(relativePoint);
		Point absolutePoint = _layout.getAbsolutePointForCharIndex(index);
		_charIndex = index;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		if (changeLastColumn) {
			_lastColumn = relativePoint.getX();
		}
	}
	
	public void setAbsolutePoint(Point absolutePoint, boolean changeLastColumn) throws InvalidLocationException {
		int index = _layout.getCharIndexForAbsolutePoint(absolutePoint);
		Point relativePoint = _layout.getRelativePointForCharIndex(index);
		_charIndex = index;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		if (changeLastColumn) {
			_lastColumn = relativePoint.getX();
		}
	}
}
