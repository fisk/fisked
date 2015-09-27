package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;

public class VisualModeSwitchResponder implements IRawInputResponder {
	private BufferWindow _window;
	
	public VisualModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.getCharacter() == 'v') {
			_window.switchToVisualMode();
			return true;
		}
		return false;
	}

}
