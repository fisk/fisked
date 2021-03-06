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
package org.fisked.buffer;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.text.IBufferDecorator;
import org.fisked.ui.screen.Screen;
import org.fisked.ui.window.IWindowManager;
import org.fisked.util.concurrency.Dispatcher;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferTextState implements CharSequence {
	private final static Logger LOG = LoggerFactory.getLogger(BufferTextState.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(BufferTextState.class);

	private final String _string;
	private volatile AttributedString _attributedString;
	private volatile TextTransition _next;
	private BufferTextState _previous;
	private volatile boolean _decorationRequested = false;
	private volatile boolean _decorationFinished = false;

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

	public AttributedString decorate(IBufferDecorator decorator) {
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
					try (IBehaviorConnection<IWindowManager> windowManagerBC = BEHAVIORS
							.getBehaviorConnection(IWindowManager.class).get()) {
						Screen currentScreen = windowManagerBC.getBehavior().getPrimaryScreen();
						currentScreen.setNeedsFullRedraw();
						currentScreen.draw();
					} catch (Exception e) {
						LOG.error("Could not decorate state: ", e);
					}
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

		if (result != null) {
			LOG.debug("State decoration apply deltas");
			result = last.applyDeltas(result.copy());
		} else {
			LOG.debug("State decoration no decoration");
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
