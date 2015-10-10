package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.RecognitionState;
import org.fisked.text.TextNavigator;

public class BasicNavigationResponder implements IInputResponder {
	private BufferWindow _window;
	private TextNavigator _navigator;
	private InputResponderChain _responders = new InputResponderChain();

	public BasicNavigationResponder(BufferWindow window) {
		_window = window;
		_navigator = new TextNavigator(_window.getBuffer());
		_responders.addResponder("gg", () -> {
			_navigator.moveToEnd();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("G", () -> {
			_navigator.moveToStart();
			_navigator.scrollUpIfNeeded();
		});
		_responders.addResponder("e", () -> {
			_navigator.scrollDown();
			_navigator.moveCursorDownIfNeeded();
		});
		_responders.addResponder("y", () -> {
			_navigator.scrollUp();
			_navigator.moveCursorUpIfNeeded();
		});
		_responders.addResponder("h", () -> {
			_navigator.moveLeft();
			_navigator.scrollUpIfNeeded();
		});
		_responders.addResponder("l", () -> {
			_navigator.moveRight();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("j", () -> {
			_navigator.moveDown();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("k", () -> {
			_navigator.moveUp();
			_navigator.scrollUpIfNeeded();
		});
		_responders.addResponder("0", () -> {
			_navigator.moveToTheBeginningOfLine();
			_navigator.scrollUpIfNeeded();
		});
		_responders.addResponder("$", () -> {
			_navigator.moveToTheEndOfLine();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("w", () -> {
			_navigator.moveToNextWord();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("e", () -> {
			_navigator.moveToEndOfWord();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("b", () -> {
			_navigator.moveToPreviousWord();
			_navigator.scrollUpIfNeeded();
		});
	}

	@Override
	public RecognitionState handleInput(Event input) {
		return _responders.handleInput(input);
	}

}
