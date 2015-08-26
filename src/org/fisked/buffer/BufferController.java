package org.fisked.buffer;

import org.fisked.buffer.drawing.Point;
import org.fisked.buffer.drawing.Range;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.log.Log;
import org.fisked.text.TextLayout;

public class BufferController {
	private Buffer _buffer;
	private BufferView _bufferView;
	private TextLayout _layout;
	
	public BufferController(Buffer buffer, BufferView bufferView) {
		_buffer = buffer;
		_bufferView = bufferView;
		_layout = new TextLayout(_buffer.getStringBuilder(), bufferView.getClippingRect().getSize().getWidth());
	}
	
	public Buffer getBuffer() {
		return _buffer;
	}
	
	public BufferView getBufferView() {
		return _bufferView;
	}
	
	public String getString(Rectangle clipRect) {
		int offsetY = clipRect.getOrigin().getY();
		int maxY = offsetY + clipRect.getSize().getHeight();
		Range range = new Range(offsetY, maxY);
		String str = _layout.getLogicalString(range);
		Log.println("str: " + str);
		Log.println("buf: " + _buffer.getStringBuilder().toString());
		return str;
	}
	
	public Point getLogicalPointAtIndex(int index) {
		return _layout.getLogicalPointForCharIndex(index, _bufferView.getClippingRect());
	}
}
