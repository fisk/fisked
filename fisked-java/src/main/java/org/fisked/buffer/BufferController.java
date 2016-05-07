package org.fisked.buffer;

import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.Size;
import org.fisked.renderingengine.service.models.selection.Selection;
import org.fisked.renderingengine.service.models.selection.SelectionMode;
import org.fisked.text.TextLayout;

public class BufferController {
	private Buffer _buffer;
	private final BufferView _bufferView;
	private TextLayout _layout;
	private final Size _size;
	private Selection _selection;

	public Selection getSelection() {
		return _selection;
	}

	public void setSelection(Selection selection) {
		_selection = selection;
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
		Selection selection = getSelection();
		if (selection == null)
			return null;
		Range range = selection.getRange();
		if (selection.getMode() != SelectionMode.NORMAL_MODE)
			throw new RuntimeException("Not yet implemented");
		CharSequence result = getBuffer().getCharSequence().subSequence(range.getStart(), range.getEnd());
		return result.toString();
	}

	public void setSelectionText(String text) {
		Selection selection = getSelection();
		Range range = selection.getRange();
		if (selection.getMode() != SelectionMode.NORMAL_MODE)
			throw new RuntimeException("Not yet implemented");
		getBuffer().removeCharsInRangeLogged(range);
		getBuffer().appendStringAtPointLogged(text);
		setSelection(null);
	}
}
