package org.fisked.text;

import org.fisked.buffer.Buffer;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.text.TextLayout.OutOfBoundsException;

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
		} catch (OutOfBoundsException e) {}
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
		Rectangle newRect = new Rectangle(new Point(y, rect.getOrigin().getX()), rect.getSize());
		_layout.setClippingRect(newRect);
	}

	public void scrollDown() {
		Rectangle rect = _layout.getClippingRect();
		int y = rect.getOrigin().getY() + 1;
		Rectangle newRect = new Rectangle(new Point(y, rect.getOrigin().getX()), rect.getSize());
		_layout.setClippingRect(newRect);
	}

	public void moveLeft() {
		setIndex(getIndex() - 1, true);
	}

	public void moveRight() {
		setIndex(getIndex() + 1, true);
	}

	public void moveDown() {
		Point point = getAbsolutePoint();
		Point newPoint = new Point(point.getY() + 1, getLastColumn());
		setAbsolutePoint(newPoint, false);
	}

	public void moveUp() {
		Point point = getAbsolutePoint();
		Point newPoint = new Point(point.getY() - 1, getLastColumn());
		setAbsolutePoint(newPoint, false);
	}
}
