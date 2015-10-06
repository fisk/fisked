package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.text.TextNavigator;

public class InputModeSwitchResponder implements IInputResponder {
	private BufferWindow _window;
	private TextNavigator _navigator;
	
	public InputModeSwitchResponder(BufferWindow window) {
		_window = window;
		_navigator = new TextNavigator(_window.getBuffer());
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.isCharacter('i')) {
			_window.switchToInputMode();
			return true;
		}
		if (input.isCharacter('a')) {
			_navigator.moveRight();
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode();
			return true;
		}
		if (input.isCharacter('A')) {
			_navigator.moveToTheEndOfLine();
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode();
			return true;
		}
		if (input.isCharacter('o')) {
			_navigator.moveToTheEndOfLine();
			_window.getBuffer().appendCharAtPoint('\n');
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode();
			return true;
		}
		if (input.isCharacter('O')) {
			_navigator.moveToTheBeginningOfLine();
			_window.getBuffer().appendCharAtPoint('\n');
			_navigator.moveLeft();
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode();
			return true;
		}
		return false;
	}

}
