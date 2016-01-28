package org.fisked.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.text.TextLayout.InvalidLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextNavigator {
	private final TextLayout _layout;
	private final Buffer _buffer;
	private final BufferWindow _window;

	private final static Logger LOG = LoggerFactory.getLogger(TextNavigator.class);

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
			LOG.error("Invalid location: " + e.getMessage());
		}
	}

	private void setIndex(int index, boolean updateLastColumn) {
		getBuffer().getCursor().setCharIndex(index, false);
		scrollDownIfNeeded();
		scrollUpIfNeeded();
		getBuffer().getCursor().setCharIndex(index, updateLastColumn);
		_window.setNeedsFullRedraw();
	}

	public void moveToIndexAndScroll(int index) {
		setIndex(index, true);
	}

	private int getLastColumn() {
		return getBuffer().getCursor().getLastColumn();
	}

	public TextNavigator(BufferWindow window) {
		_window = window;
		_buffer = window.getBuffer();
		_layout = _buffer.getTextLayout();
	}

	public void scrollUp() {
		Rectangle rect = _layout.getClippingRect();

		// TODO: Figure out the escape sequences for scrolling
		// ServiceManager sm = ServiceManager.getInstance();
		// int start = rect.getOrigin().getY();
		// int end = start + rect.getSize().getHeight();
		// sm.getConsoleService().scrollTextRegionUp(new Range(start, end));

		int y = Math.max(rect.getOrigin().getY() - 1, 0);
		Rectangle newRect = new Rectangle(new Point(rect.getOrigin().getX(), y), rect.getSize());
		_layout.setClippingRect(newRect);
		_layout.setNeedsLayout();
	}

	public void scrollDown() {
		Rectangle rect = _layout.getClippingRect();

		// TODO: Figure out the escape sequences for scrolling
		// ServiceManager sm = ServiceManager.getInstance();
		// int start = rect.getOrigin().getY();
		// int end = start + rect.getSize().getHeight();
		// sm.getConsoleService().scrollTextRegionDown(new Range(start, end));

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
		if (newIndex <= _buffer.getCharSequence().length()) {
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
		if (newIndex == 0) {
			return;
		}
		if (!String.valueOf(_buffer.getCharSequence().charAt(newIndex - 1)).matches(".")) {
			return;
		}
		newIndex--;
		while (newIndex >= 0 && String.valueOf(_buffer.getCharSequence().charAt(newIndex)).matches(".")) {
			newIndex--;
		}
		setIndex(++newIndex, true);
	}

	public void moveToTheEndOfLine() {
		int newIndex = getIndex();
		if (newIndex == _buffer.getCharSequence().length()) {
			return;
		}
		if (!String.valueOf(_buffer.getCharSequence().charAt(newIndex)).matches(".")) {
			return;
		}
		newIndex++;
		while (newIndex < _buffer.getCharSequence().length()
				&& String.valueOf(_buffer.getCharSequence().charAt(newIndex)).matches(".")) {
			newIndex++;
		}
		setIndex(newIndex, true);
	}

	Pattern _nextWordPattern = Pattern.compile("\\s([^\\s]+)");

	public void moveToNextWord() {
		int index = getIndex();
		CharSequence string = _buffer.getCharSequence();
		Matcher matcher = _nextWordPattern.matcher(string);
		if (matcher.find(index)) {
			int newIndex = matcher.start(1);
			setIndex(newIndex, true);
		}
	}

	Pattern _endOfWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");

	public void moveToEndOfWord() {
		int index = getIndex() + 1;
		CharSequence string = _buffer.getCharSequence();
		if (index >= string.length())
			return;
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
		CharSequence string = _buffer.getCharSequence();
		StringBuilder reverse = new StringBuilder(_buffer.getCharSequence()).reverse();
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
			_window.setNeedsFullRedraw();
		}
	}

	public void scrollDownIfNeeded() {
		Point point = getBuffer().getCursor().getAbsolutePoint();
		Rectangle rect = _layout.getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			scrollDown();
			point = getBuffer().getCursor().getAbsolutePoint();
			rect = _layout.getClippingRect();
			_window.setNeedsFullRedraw();
		}
	}

	public void moveToStart() {
		setIndex(0, true);
	}

	public void moveToEnd() {
		setIndex(getBuffer().getCharSequence().length(), true);
	}
}
