package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;

public class InputModeSwitchResponder implements IRawInputResponder {
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
