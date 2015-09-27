package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;

public class InputModeSwitchResponder implements IInputResponder {
	private BufferWindow _window;
	
	public InputModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.getCharacter() == 'i') {
			_window.switchToInputMode();
			return true;
		}
		return false;
	}

}
