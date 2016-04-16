package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.IRecognitionAction;
import org.fisked.responder.RecognitionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchTextResponder implements IInputResponder {
	private final BufferWindow _window;
	private boolean _isSearching = false;
	private StringBuilder _searchString = new StringBuilder();
	private boolean _isSearchingForward = true;
	private int _charIndexBefore = -1;
	private IRecognitionAction _action;

	private final static Logger LOG = LoggerFactory.getLogger(SearchTextResponder.class);

	public SearchTextResponder(BufferWindow window) {
		_window = window;
	}

	private void goForward() {
		int currentPosition = _window.getBuffer().getPointIndex();
		String str = _window.getBuffer().toString();
		int newIndex = str.indexOf(_searchString.toString(), currentPosition);
		if (newIndex == -1) {
			_window.getBuffer().getCursor().setCharIndex(_charIndexBefore, true);
		} else {
			_window.getBuffer().getCursor().setCharIndex(newIndex, true);
		}
	}

	private void goBackward() {
		int currentPosition = _window.getBuffer().getPointIndex();
		String str = _window.getBuffer().toString();
		int newIndex = str.lastIndexOf(_searchString.toString(), currentPosition);
		if (newIndex == -1) {
			_window.getBuffer().getCursor().setCharIndex(_charIndexBefore, true);
		} else {
			_window.getBuffer().getCursor().setCharIndex(newIndex, true);
		}
	}

	private void goToNextSearch() {
		if (_isSearchingForward) {
			goForward();
		} else {
			goBackward();
		}
	}

	private void goToPrevSearch() {
		if (_isSearchingForward) {
			goBackward();
		} else {
			goForward();
		}
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		if (_isSearching) {
			_action = () -> {
				if (nextEvent.isReturn()) {
					_isSearching = false;
					_window.getCommandController().setCommandFeedback(null);
					_searchString = new StringBuilder();
				} else if (nextEvent.isCharacter()) {
					char character = nextEvent.getCharacter();
					_searchString.append(character);
					goToNextSearch();
					_window.getCommandController().setCommandFeedback("/" + _searchString.toString());
				} else if (nextEvent.isEscape()) {
					_searchString = new StringBuilder();
					_isSearching = false;
					_window.getBuffer().getCursor().setCharIndex(_charIndexBefore, true);
					_window.getCommandController().setCommandFeedback(null);
					_searchString = new StringBuilder();
				}
			};
			_window.setNeedsFullRedraw();
			return RecognitionState.Recognized;
		} else {
			if (nextEvent.isCharacter('n')) {
				_action = () -> {
					goToNextSearch();
				};
				_window.setNeedsFullRedraw();
				return RecognitionState.Recognized;
			}
			if (nextEvent.isCharacter('N')) {
				_action = () -> {
					goToPrevSearch();
				};
				_window.setNeedsFullRedraw();
				return RecognitionState.Recognized;
			}
			if (nextEvent.isCharacter('/')) {
				_action = () -> {
					_isSearchingForward = true;
					_isSearching = true;
					_charIndexBefore = _window.getBuffer().getPointIndex();
					_window.getCommandController().setCommandFeedback("/");
				};
				_window.setNeedsFullRedraw();
				return RecognitionState.Recognized;
			} else if (nextEvent.isCharacter('?')) {
				_action = () -> {
					_isSearchingForward = false;
					_isSearching = true;
					_charIndexBefore = _window.getBuffer().getPointIndex();
				};
				_window.setNeedsFullRedraw();
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
