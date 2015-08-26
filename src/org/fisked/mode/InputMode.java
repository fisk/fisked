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
		if (input.isEscape()) {
			_window.switchToNormalMode();
			return true;
		}
		Buffer buffer = _window.getBuffer();
		buffer.appendCharAtPoint(input.getCharacter());
		_window.draw();
		_window.drawPoint();
		return true;
	}

	@Override
	public String getModeName() {
		return "input";
	}

}
