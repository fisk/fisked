/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
