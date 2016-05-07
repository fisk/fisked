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
package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.IRecognitionAction;
import org.fisked.responder.RecognitionState;

public class SearchTextResponder implements IInputResponder {
	private final BufferWindow _window;
	private boolean _isSearching = false;
	private StringBuilder _searchString = new StringBuilder();
	private boolean _isSearchingForward = true;
	private int _charIndexBefore = -1;
	private IRecognitionAction _action;

	public SearchTextResponder(BufferWindow window) {
		_window = window;
	}

	private void goForward(boolean next) {
		String str = _window.getBuffer().toString();
		int currentPosition = _window.getBuffer().getPointIndex();
		if (next) {
			currentPosition = Math.min(currentPosition + 1, _window.getBuffer().length());
		}
		int newIndex = str.indexOf(_searchString.toString(), currentPosition);
		if (newIndex == -1) {
			_window.getBuffer().getCursor().setCharIndex(_charIndexBefore, true);
		} else {
			_window.getBuffer().getCursor().setCharIndex(newIndex, true);
		}
	}

	private void goBackward(boolean next) {
		String str = _window.getBuffer().toString();
		int currentPosition = _window.getBuffer().getPointIndex();
		if (next) {
			currentPosition = Math.max(currentPosition - 1, 0);
		}
		int newIndex = str.lastIndexOf(_searchString.toString(), currentPosition);
		if (newIndex == -1) {
			_window.getBuffer().getCursor().setCharIndex(_charIndexBefore, true);
		} else {
			_window.getBuffer().getCursor().setCharIndex(newIndex, true);
		}
	}

	private void goToNextSearch(boolean next) {
		if (_isSearchingForward) {
			goForward(next);
		} else {
			goBackward(next);
		}
	}

	private void goToPrevSearch(boolean next) {
		if (_isSearchingForward) {
			goBackward(next);
		} else {
			goForward(next);
		}
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		if (_isSearching) {
			_action = () -> {
				if (nextEvent.isReturn()) {
					_isSearching = false;
					_window.getCommandController().setCommandFeedback(null);
				} else if (nextEvent.isCharacter()) {
					char character = nextEvent.getCharacter();
					_searchString.append(character);
					goToNextSearch(false);
					String prefix;
					if (_isSearchingForward) {
						prefix = "/";
					} else {
						prefix = "?";
					}
					_window.getCommandController().setCommandFeedback(prefix + _searchString.toString());
				} else if (nextEvent.isEscape()) {
					_searchString = new StringBuilder();
					_isSearching = false;
					_window.getBuffer().getCursor().setCharIndex(_charIndexBefore, true);
					_window.getCommandController().setCommandFeedback(null);
					_searchString = new StringBuilder();
				}
				_window.setNeedsFullRedraw();
			};
			return RecognitionState.Recognized;
		} else {
			if (nextEvent.isCharacter('n')) {
				_action = () -> {
					_charIndexBefore = _window.getBuffer().getPointIndex();
					goToNextSearch(true);
					_window.setNeedsFullRedraw();
				};
				return RecognitionState.Recognized;
			}
			if (nextEvent.isCharacter('N')) {
				_action = () -> {
					_charIndexBefore = _window.getBuffer().getPointIndex();
					goToPrevSearch(true);
					_window.setNeedsFullRedraw();
				};
				return RecognitionState.Recognized;
			}
			if (nextEvent.isCharacter('/')) {
				_action = () -> {
					_isSearchingForward = true;
					_isSearching = true;
					_charIndexBefore = _window.getBuffer().getPointIndex();
					_searchString = new StringBuilder();
					_window.getCommandController().setCommandFeedback("/");
					_window.setNeedsFullRedraw();
				};
				return RecognitionState.Recognized;
			} else if (nextEvent.isCharacter('?')) {
				_action = () -> {
					_isSearchingForward = false;
					_isSearching = true;
					_charIndexBefore = _window.getBuffer().getPointIndex();
					_searchString = new StringBuilder();
					_window.getCommandController().setCommandFeedback("?");
					_window.setNeedsFullRedraw();
				};
				return RecognitionState.Recognized;
			}
		}

		_action = null;
		return RecognitionState.NotRecognized;
	}

	@Override
	public void onRecognize() {
		IRecognitionAction action = _action;
		if (action != null) {
			action.onRecognize();
		}
	}

}
