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
		if (input.getCharacter() == 'h') {
			_navigator.moveLeft();
		} else if (input.getCharacter() == 'l') {
			_navigator.moveRight();
		} else if (input.getCharacter() == 'j') {
			_navigator.moveDown();
		} else if (input.getCharacter() == 'k') {
			_navigator.moveUp();
		} else {
			return false;
		}
		return true;
	}

}
