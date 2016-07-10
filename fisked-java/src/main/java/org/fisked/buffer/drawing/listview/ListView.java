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
package org.fisked.buffer.drawing.listview;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.InputResponderChain;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListView<T> extends View {
	private final static Logger LOG = LoggerFactory.getLogger(ListView.class);
	private ListViewDataSource<T> _dataSource;
	private ListViewDelegate<T> _delegate;

	private int _selectedIndex = 0;
	private final int _topIndex = 0;

	public interface ListViewDataSource<T> {
		int length();

		T get(int index);
	}

	public interface ListViewDelegate<T> {
		int getElementLines();

		void selectedItem(int index);

		AttributedString toString(T element, boolean selected);
	}

	public ListView(Rectangle frame) {
		super(frame);
	}

	@Override
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		int length = _dataSource.length();
		int linesPerElement = _delegate.getElementLines();
		LOG.debug("Drawing " + length + " lines, " + linesPerElement + " lines per element.");
		for (int i = _topIndex; i < length; i++) {
			int row = linesPerElement * (i - _topIndex);
			if (row > drawingRect.getSize().getHeight()) {
				return;
			}
			T element = _dataSource.get(i);
			boolean selected = i == _selectedIndex;
			AttributedString text = _delegate.toString(element, selected);
			LOG.debug("Element" + i + ", row: " + row + ", text: " + text.toString());
			context.moveTo(drawingRect.getOrigin().getX(), row);
			context.printString(text);
		}
	}

	private void moveDown() {
		if (_selectedIndex + 1 < _dataSource.length()) {
			_selectedIndex++;
		}
	}

	private void moveUp() {
		if (_selectedIndex > 0) {
			_selectedIndex--;
		}
	}

	public InputResponderChain createResponder() {
		InputResponderChain chain = new InputResponderChain();
		chain.addResponder("j", () -> {
			moveDown();
		});
		chain.addResponder("k", () -> {
			moveUp();
		});
		chain.addResponder("e", () -> {
			_delegate.selectedItem(_selectedIndex);
		});
		return chain;
	}

	public void setDataSource(ListViewDataSource<T> dataSource) {
		_dataSource = dataSource;
	}

	public ListViewDataSource<T> getDataSource() {
		return _dataSource;
	}

	public void setDelegate(ListViewDelegate<T> delegate) {
		_delegate = delegate;
	}

	public ListViewDelegate<T> getDelegate() {
		return _delegate;
	}
}
