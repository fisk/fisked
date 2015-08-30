package org.fisked.mode;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;

public class InputMode extends AbstractMode {
	
	public InputMode(BufferWindow window) {
		super(window);
	}

	@Override
	public boolean handleInput(Event input) {
		try {
			if (input.isBackspace()) {
				_window.getBuffer().removeCharAtPoint();
				return true;
			} else if (input.isEscape()) {
				_window.switchToNormalMode();
				return true;
			}
			Buffer buffer = _window.getBuffer();
			buffer.appendCharAtPoint(input.getCharacter());
			return true;
		} finally {
			_window.refresh();
		}
	}

	@Override
	public String getModeName() {
		return "input";
	}

}
