/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
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
package org.fisked.ui.listview;

import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.Event;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.RecognitionState;
import org.fisked.ui.drawing.View;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Point;
import org.fisked.util.models.Rectangle;
import org.fisked.util.models.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListView<T> extends View {
	private final static Logger LOG = LoggerFactory.getLogger(ListView.class);
	private ListViewDataSource<T> _dataSource;
	private ListViewDelegate<T> _delegate;

	private int _selectedIndex = 0;
	private int _topIndex = 0;

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
		adjustSelectedIndex();
		int length = _dataSource.length();
		int linesPerElement = _delegate.getElementLines();
		LOG.debug("Drawing " + length + " lines, " + linesPerElement + " lines per element.");
		for (int i = _topIndex; i < length; i++) {
			int row = linesPerElement * (i - _topIndex);
			if (row > drawingRect.getSize().getHeight()) {
				LOG.debug("Drawing exit: " + row + ", " + drawingRect.getSize().getHeight());
				return;
			}
			T element = _dataSource.get(i);
			boolean selected = i == _selectedIndex;
			AttributedString text = _delegate.toString(element, selected);
			LOG.debug("Element " + i + ", row: " + row + ", text: " + text.toString());
			context.printString(new Point(0, row), text);
		}
	}

	private void adjustSelectedIndex() {
		int length = _dataSource.length();
		if (_selectedIndex >= length) {
			_selectedIndex = length - 1;
		}
		if (_selectedIndex < 0) {
			_selectedIndex = 0;
		}
	}

	private void adjustTopIndex() {
		int linesPerElement = _delegate.getElementLines();
		int selectedStart = linesPerElement * (_selectedIndex - _topIndex);
		int selectedEnd = linesPerElement * (_selectedIndex - _topIndex + 1) - 1;

		Size size = getFrame().getSize();

		if (selectedEnd > size.getHeight()) {
			_topIndex++;
		} else if (selectedStart < 0) {
			_topIndex--;
		}
	}

	private void moveDown() {
		LOG.debug("Moving down " + _selectedIndex);
		if (_selectedIndex + 1 < _dataSource.length()) {
			_selectedIndex++;
		}
		adjustTopIndex();
		LOG.debug("Moved down " + _selectedIndex);
	}

	private void moveUp() {
		LOG.debug("Moving up " + _selectedIndex);
		if (_selectedIndex > 0) {
			_selectedIndex--;
		}
		adjustTopIndex();
		LOG.debug("Moved up " + _selectedIndex);
	}

	public InputResponderChain createResponder() {
		InputResponderChain chain = new InputResponderChain();
		chain.addResponder((Event event) -> {
			if (event.isControlChar('j')) {
				return RecognitionState.Recognized;
			} else {
				return RecognitionState.NotRecognized;
			}
		}, () -> {
			moveDown();
		});
		chain.addResponder((Event event) -> {
			if (event.isControlChar('k')) {
				return RecognitionState.Recognized;
			} else {
				return RecognitionState.NotRecognized;
			}
		}, () -> {
			moveUp();
		});
		chain.addResponder((Event event) -> {
			if (event.isReturn()) {
				return RecognitionState.Recognized;
			} else {
				return RecognitionState.NotRecognized;
			}
		}, () -> {
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

	public void drawPoint(IRenderingContext context) {
		int linesPerElement = _delegate.getElementLines();
		int row = linesPerElement * (_selectedIndex - _topIndex);
		Point point = new Point(0, row);
		point = point.addedBy(getClippingRect().getOrigin());
		context.moveTo(point);
	}
}
