package org.fisked.mode.responder;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;

public class TextInputResponder implements IInputRecognizer {
	private final BufferWindow _window;

	public TextInputResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (input.isBackspace()) {
			_window.getBuffer().removeCharAtPoint();
			_window.setNeedsFullRedraw();
			return RecognitionState.Recognized;
		}
		Buffer buffer = _window.getBuffer();
		if (input.isReturn()) {
			buffer.appendCharAtPoint('\n');
		} else {
			buffer.appendCharAtPoint(input.getCharacter());
		}
		_window.setNeedsFullRedraw();
		return RecognitionState.Recognized;
	}

}
