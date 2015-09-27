package org.fisked.text;

import org.fisked.buffer.Buffer;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;

// TODO: This file needs a lot more logic

public class TextNavigator {
	private int lastColumn;
	private TextLayout _layout;
	private Buffer _buffer;
	
	private Buffer getBuffer() {
		return _buffer;
	}
	
	private int getIndex() {
		return getBuffer().getPointIndex();
	}
	
	private Point getPoint() {
		return _layout.getLogicalPointForCharIndex(getIndex());
	}
	
	private void setIndex(int index) {
		getBuffer().setPointIndex(index);
	}
	
	public TextNavigator(Buffer buffer, TextLayout layout) {
		_buffer = buffer;
		_layout = layout;
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
		setIndex(getIndex() - 1);
	}
	
	public void moveRight() {
		setIndex(getIndex() + 1);
	}
	
	public void moveDown() {
		Buffer buff = getBuffer();
		int pos = getIndex();
		int offset = 0;
		while (pos >= 0) {
			if (buff.getStringBuilder().charAt(pos) == '\n') {
				offset = getIndex() - pos;
				break;
			}
			pos--;
		}
		if (pos == -1) { offset = getIndex() + 1; }
		int lineEndsAt = buff.getStringBuilder().indexOf("\n", getIndex());
		if (lineEndsAt != -1) {
			buff.setPointIndex(lineEndsAt + offset);
		}
	}
	
	public void moveUp() {
		Buffer buff = getBuffer();
		int pos = getIndex();
		int offset = 0;
		boolean foundFirstLinebreak = false;
		while (pos >= 0) {
			if (buff.getStringBuilder().charAt(pos) == '\n') {
				if (!foundFirstLinebreak) {
					offset = getIndex() - pos;
					foundFirstLinebreak = true;
				} else {
					break;
				}
			}
			pos--;
		}

		setIndex(pos + offset);
	}
}
