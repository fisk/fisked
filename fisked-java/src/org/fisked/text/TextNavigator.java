package org.fisked.text;

import org.fisked.buffer.Buffer;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;

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
	
	public TextNavigator(Buffer buffer) {
		_buffer = buffer;
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
	
	public void moveUp() {
		
	}
	
	public void moveDown() {
		
	}
	
	public void moveLeft() {
		
	}
	
	public void moveRight() {
		
	}
}
