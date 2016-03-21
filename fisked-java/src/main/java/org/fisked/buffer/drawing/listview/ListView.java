package org.fisked.buffer.drawing.listview;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.InputResponderChain;
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
