package org.fisked.buffer;

import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.Size;
import org.fisked.text.TextLayout;

public class BufferController {
	private Buffer _buffer;
	private final BufferView _bufferView;
	private TextLayout _layout;
	private final Size _size;
	private Range _selection;

	public Range getSelection() {
		return _selection;
	}

	public void setSelection(Range range) {
		_selection = range;
	}

	public BufferController(BufferView bufferView, Size size) {
		_bufferView = bufferView;
		_size = size;
		setBuffer(new Buffer());
	}

	public TextLayout getTextLayout() {
		return _layout;
	}

	public Buffer getBuffer() {
		return _buffer;
	}

	public BufferView getBufferView() {
		return _bufferView;
	}

	public Point getLogicalPoint() {
		int index = _buffer.getPointIndex();
		return _layout.getRelativeLogicalPointForCharIndex(index);
	}

	public void setBuffer(Buffer buffer) {
		_buffer = buffer;
		_layout = new TextLayout(_buffer, _size);
		_buffer.setTextLayout(_layout);
	}

	public interface IStringDecorator {
		void draw(Point point, String string, int offset);
	}

	public void drawBuffer(Rectangle drawingRect, IStringDecorator decorator) {
		_layout.getLogicalString((int offset, String line, Point relativePoint, boolean physicalLine) -> {
			decorator.draw(relativePoint, line, offset);
		});
	}

	public String getSelectedText() {
		Range selection = getSelection();
		if (selection == null)
			return null;
		CharSequence result = getBuffer().getCharSequence().subSequence(selection.getStart(), selection.getEnd());
		return result.toString();
	}

	public void setSelectionText(String text) {
		Range selection = getSelection();
		getBuffer().removeCharsInRangeLogged(selection);
		getBuffer().appendStringAtPointLogged(text);
		setSelection(null);
	}
}
