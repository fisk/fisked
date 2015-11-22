package org.fisked.buffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fisked.Application;
import org.fisked.buffer.drawing.Window;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.text.ITextDecorator;
import org.fisked.util.concurrency.Dispatcher;

public class BufferTextState implements CharSequence {
	private final String _string;
	private volatile AttributedString _attributedString;
	private volatile TextTransition _next;
	private BufferTextState _previous;
	private volatile boolean _decorationRequested = false;
	private volatile boolean _decorationFinished = false;

	final static Logger LOG = LogManager.getLogger(BufferTextState.class);

	private abstract class TextTransition {
		private final BufferTextState _next;

		public TextTransition(BufferTextState next) {
			_next = next;
		}

		public BufferTextState getNext() {
			return _next;
		}

		public abstract AttributedString apply(AttributedString string);
	}

	public AttributedString getAttributedString() {
		return _attributedString;
	}

	public void setAttributedString(AttributedString attributedString) {
		_attributedString = attributedString;
	}

	public AttributedString decorate(ITextDecorator decorator) {
		LOG.debug("State decoration requested");
		if (!_decorationRequested) {
			LOG.debug("State decoration requesting decoration");
			_decorationRequested = true;
			decorator.decorate(this, attrStr -> {
				LOG.debug("State decoration got decoration");
				_attributedString = attrStr;
				_decorationFinished = true;
				Dispatcher.getInstance().runMain(() -> {
					LOG.debug("State decoration pushed to main");
					Window currentWindow = Application.getApplication().getPrimaryWindow();
					currentWindow.setNeedsFullRedraw();
					currentWindow.draw();
				});
			});
		}

		if (_attributedString != null)
			return _attributedString;

		LOG.debug("State decoration no cache");

		AttributedString result = null;

		BufferTextState last = this;
		while (last._previous != null) {
			last = last._previous;
			if (last._decorationFinished) {
				_previous = last;
				last._previous = null;
				result = last._attributedString;
			}
		}

		LOG.debug("State decoration something");

		if (result != null) {
			LOG.debug("State decoration apply deltas");
			result = last.applyDeltas(result.copy());
		} else {
			LOG.debug("State decoration no recoration");
			result = new AttributedString(_string);
		}
		LOG.debug("State decoration result");

		return result;
	}

	private class InsertTextTransition extends TextTransition {
		private final String _string;
		private final int _index;

		public InsertTextTransition(BufferTextState next, String string, int index) {
			super(next);
			_string = string;
			_index = index;
		}

		@Override
		public AttributedString apply(AttributedString string) {
			return string.stringByInsertingString(_string, _index);
		}
	}

	private class DeleteTextTransition extends TextTransition {
		private final Range _range;

		public DeleteTextTransition(BufferTextState next, Range range) {
			super(next);
			_range = range;
		}

		@Override
		public AttributedString apply(AttributedString string) {
			return string.stringByDeletingString(_range);
		}
	}

	public BufferTextState(String string, BufferTextState previous) {
		_string = string;
		_previous = previous;
	}

	public BufferTextState insertString(int index, String string) {
		StringBuilder builder = new StringBuilder(_string);
		builder.insert(index, string);
		BufferTextState state = new BufferTextState(builder.toString(), this);

		TextTransition transition = new InsertTextTransition(state, string, index);
		_next = transition;
		return state;
	}

	public BufferTextState deleteString(Range range) {
		StringBuilder builder = new StringBuilder(_string);
		builder.delete(range.getStart(), range.getEnd());
		BufferTextState state = new BufferTextState(builder.toString(), this);

		TextTransition transition = new DeleteTextTransition(state, range);
		_next = transition;
		return state;
	}

	public AttributedString applyDeltas(AttributedString start) {
		TextTransition current = _next;
		AttributedString result = start;
		LOG.debug("Apply delta start: " + start.toString());
		while (current != null) {
			result = current.apply(result);
			LOG.debug("Apply delta step: " + result.toString());
			current = current.getNext()._next;
		}
		LOG.debug("Apply delta done: " + result.toString());
		return result;
	}

	@Override
	public String toString() {
		return _string;
	}

	@Override
	public int length() {
		return _string.length();
	}

	@Override
	public char charAt(int index) {
		return _string.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return _string.subSequence(start, end);
	}

}
