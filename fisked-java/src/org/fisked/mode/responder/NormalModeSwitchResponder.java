package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;

public class NormalModeSwitchResponder implements IRawInputResponder {
	private BufferWindow _window;
	
	public NormalModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.isEscape()) {
			_window.switchToNormalMode();
			return true;
		}
		return false;
	}

}
