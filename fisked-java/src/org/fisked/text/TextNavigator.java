package org.fisked.text;

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
			Log.println(e.getMessage());
		}
	}

	private void setIndex(int index, boolean updateLastColumn) {
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
}
