package org.fisked.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.buffer.Buffer;
import org.fisked.log.Log;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.text.TextLayout.InvalidLocationException;

public class TextNavigator {
	private TextLayout _layout;
	private Buffer _buffer;

	private Buffer getBuffer() {
		return _buffer;
	}

	private int getIndex() {
		return getBuffer().getPointIndex();
	}

	private Point getAbsolutePoint() {
		return getBuffer().getCursor().getAbsolutePoint();
	}
	
	private void setAbsolutePoint(Point point, boolean updateLastColumn) {
		try {
			getBuffer().getCursor().setAbsolutePoint(point, updateLastColumn);
		} catch (InvalidLocationException e) {
			Log.println("Invalid location: " + e.getMessage());
		}
	}

	private void setIndex(int index, boolean updateLastColumn) {
		Log.println("Set Index: " + index);
		getBuffer().getCursor().setCharIndex(index, updateLastColumn);
	}
	
	private int getLastColumn() {
		return getBuffer().getCursor().getLastColumn();
	}

	public TextNavigator(Buffer buffer) {
		_buffer = buffer;
		_layout = _buffer.getTextLayout();
	}

	public void scrollUp() {
		Rectangle rect = _layout.getClippingRect();
		int y = Math.max(rect.getOrigin().getY() - 1, 0);
		Rectangle newRect = new Rectangle(new Point(rect.getOrigin().getX(), y), rect.getSize());
		_layout.setClippingRect(newRect);
		_layout.setNeedsLayout();
	}

	public void scrollDown() {
		Rectangle rect = _layout.getClippingRect();
		int y = rect.getOrigin().getY() + 1;
		Rectangle newRect = new Rectangle(new Point(rect.getOrigin().getX(), y), rect.getSize());
		_layout.setClippingRect(newRect);
		_layout.setNeedsLayout();
	}

	public void moveLeft() {
		int newIndex = getIndex() - 1;
		if (newIndex >= 0) {
			setIndex(newIndex, true);
		}
	}

	public void moveRight() {
		int newIndex = getIndex() + 1;
		if (newIndex <= _buffer.getStringBuilder().length()) {
			setIndex(newIndex, true);
		}
	}

	public void moveDown() {
		Point point = getAbsolutePoint();
		Point newPoint = new Point(getLastColumn(), point.getY() + 1);
		setAbsolutePoint(newPoint, false);
	}

	public void moveUp() {
		Point point = getAbsolutePoint();
		Point newPoint = new Point(getLastColumn(), point.getY() - 1);
		setAbsolutePoint(newPoint, false);
	}
	
	public void moveToTheBeginningOfLine() {
		int newIndex = getIndex();
		if (newIndex == 0) { return; }
		if (!String.valueOf(_buffer.getStringBuilder().charAt(newIndex - 1)).matches(".")) {
			return;
		}
		newIndex--;
		while (newIndex >= 0 && String.valueOf(_buffer.getStringBuilder().charAt(newIndex)).matches(".")) {
			newIndex--;
		}
		setIndex(++newIndex, true);
	}

	public void moveToTheEndOfLine() {
		int newIndex = getIndex();
		if (newIndex == _buffer.getStringBuilder().length()) { return; }
		if (!String.valueOf(_buffer.getStringBuilder().charAt(newIndex)).matches(".")) {
			return;
		}
		newIndex++;
		while (newIndex < _buffer.getStringBuilder().length() && String.valueOf(_buffer.getStringBuilder().charAt(newIndex)).matches(".")) {
			newIndex++;
		}
		setIndex(newIndex, true);
	}

	Pattern _nextWordPattern = Pattern.compile("\\s([^\\s]+)");
	public void moveToNextWord() {
		int index = getIndex();
		CharSequence string = _buffer.getStringBuilder();
		Matcher matcher = _nextWordPattern.matcher(string);
		if (matcher.find(index)) {
			int newIndex = matcher.start(1);
			setIndex(newIndex, true);
		}
	}

	Pattern _endOfWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");
	public void moveToEndOfWord() {
		int index = getIndex() + 1;
		CharSequence string = _buffer.getStringBuilder();
		if (index >= string.length()) return;
		Matcher matcher = _endOfWordPattern.matcher(string);
		if (matcher.find(index)) {
			int newIndex = matcher.end(1) - 1;
			if (newIndex > 0) {
				setIndex(newIndex, true);
			}
		}
	}

	Pattern _previousWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");
	public void moveToPreviousWord() {
		int index = getIndex();
		CharSequence string = _buffer.getStringBuilder();
		StringBuilder reverse = new StringBuilder(_buffer.getStringBuilder()).reverse();
		int length = string.length();
		Matcher matcher = _previousWordPattern.matcher(reverse);
		if (matcher.find(length - index)) {
			int newIndex = matcher.end(1);
			setIndex(length - newIndex, true);
		}
	}
	
	public void moveCursorDownIfNeeded() {
		Point point = getBuffer().getCursor().getAbsolutePoint();
		while (point.getY() < getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
			moveDown();
			point = getBuffer().getCursor().getAbsolutePoint();
		}
	}
	
	public void moveCursorUpIfNeeded() {
		Point point = getBuffer().getCursor().getAbsolutePoint();
		Rectangle rect = _layout.getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			moveUp();
			point = getBuffer().getCursor().getAbsolutePoint();
			rect = _layout.getClippingRect();
		}
	}
	
	public void scrollUpIfNeeded() {
		Point point = getBuffer().getCursor().getAbsolutePoint();
		while (point.getY() < getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
			scrollUp();
			point = getBuffer().getCursor().getAbsolutePoint();
		}
	}
	
	public void scrollDownIfNeeded() {
		Point point = getBuffer().getCursor().getAbsolutePoint();
		Rectangle rect = _layout.getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			scrollDown();
			point = getBuffer().getCursor().getAbsolutePoint();
			rect = _layout.getClippingRect();
		}
	}

	public void moveToStart() {
		setIndex(0, true);
	}

	public void moveToEnd() {
		setIndex(getBuffer().getStringBuilder().length(), true);
	}
}
