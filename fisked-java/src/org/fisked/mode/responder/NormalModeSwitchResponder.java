package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;

public class NormalModeSwitchResponder implements IInputResponder {
	private BufferWindow _window;
	
	public NormalModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState handleInput(Event input) {
		if (input.isEscape()) {
			_window.switchToNormalMode();
			return RecognitionState.Recognized;
		}
		return RecognitionState.NotRecognized;
	}

}
