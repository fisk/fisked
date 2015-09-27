package org.fisked.mode.responder;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;

public class TextInputResponder implements IRawInputResponder {
	private BufferWindow _window;
	
	public TextInputResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.isBackspace()) {
			_window.getBuffer().removeCharAtPoint();
			return true;
		}
		Buffer buffer = _window.getBuffer();
		if (input.isReturn()) {
			buffer.appendCharAtPoint('\n');
		} else {
			buffer.appendCharAtPoint(input.getCharacter());
		}
		return true;
	}

}
