package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;

public class VisualModeSwitchResponder implements IInputResponder {
	private BufferWindow _window;
	
	public VisualModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.isCharacter('v')) {
			_window.switchToVisualMode();
			return true;
		}
		return false;
	}

}
