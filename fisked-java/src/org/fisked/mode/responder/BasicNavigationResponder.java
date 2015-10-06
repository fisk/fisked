package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.text.TextNavigator;

public class BasicNavigationResponder implements IInputResponder {
	private BufferWindow _window;
	private TextNavigator _navigator;
	
	public BasicNavigationResponder(BufferWindow window) {
		_window = window;
		_navigator = new TextNavigator(_window.getBuffer());
	}
	
	@Override
	public boolean handleInput(Event input) {
		if (input.isControlChar('e')) {
			_navigator.scrollDown();
			_navigator.moveCursorDownIfNeeded();
		} else if (input.isControlChar('y')) {
			_navigator.scrollUp();
			_navigator.moveCursorUpIfNeeded();
		} else if (input.isCharacter('h')) {
			_navigator.moveLeft();
			_navigator.scrollUpIfNeeded();
		} else if (input.isCharacter('l')) {
			_navigator.moveRight();
			_navigator.scrollDownIfNeeded();
		} else if (input.isCharacter('j')) {
			_navigator.moveDown();
			_navigator.scrollDownIfNeeded();
		} else if (input.isCharacter('k')) {
			_navigator.moveUp();
			_navigator.scrollUpIfNeeded();
		} else if (input.isCharacter('0')) {
			_navigator.moveToTheBeginningOfLine();
			_navigator.scrollUpIfNeeded();
		} else if (input.isCharacter('$')) {
			_navigator.moveToTheEndOfLine();
			_navigator.scrollDownIfNeeded();
		} else if (input.isCharacter('w')) {
			_navigator.moveToNextWord();
			_navigator.scrollDownIfNeeded();
		} else if (input.isCharacter('e')) {
			_navigator.moveToEndOfWord();
			_navigator.scrollDownIfNeeded();
		} else if (input.isCharacter('b')) {
			_navigator.moveToPreviousWord();
			_navigator.scrollUpIfNeeded();
		} else {
			return false;
		}
		return true;
	}

}
