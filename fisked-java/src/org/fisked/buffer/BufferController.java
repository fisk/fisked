package org.fisked.buffer;

import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.Size;
import org.fisked.text.TextLayout;

public class BufferController {
	private Buffer _buffer;
	private BufferView _bufferView;
	private TextLayout _layout;
	
	public BufferController(BufferView bufferView, Size size) {
		_buffer = new Buffer();
		_bufferView = bufferView;
		_layout = new TextLayout(_buffer.getStringBuilder(), size);
	}
	
	public Buffer getBuffer() {
		return _buffer;
	}
	
	public BufferView getBufferView() {
		return _bufferView;
	}
	
	public String getString(Rectangle clipRect) {
		String str = _layout.getLogicalString();
		return str;
	}
	
	public Point getLogicalPoint() {
		int index = _buffer.getPointIndex();
		return _layout.getLogicalPointForCharIndex(index);
	}

	public void setBuffer(Buffer buffer) {
		_buffer = buffer;
		_layout.setText(_buffer.getStringBuilder());
	}
	
	public interface IStringDecorator {
		void draw(Point point, String string, int offset);
	}
	
	public void drawBuffer(Rectangle drawingRect, IStringDecorator decorator) {
		String string = getString(drawingRect);
		String[] lines = string.split("\n");
		int i = 0;
		int offset = 0;
		for (String line : lines) {
			Point point = new Point(drawingRect.getOrigin().getX(), drawingRect.getOrigin().getY() + i);
			decorator.draw(point, line, offset);
			offset += line.length() + 1;
			i++;
		}
	}
}
