package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;

public class VisualModeSwitchResponder implements IInputResponder {
	private BufferWindow _window;
	
	public VisualModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState handleInput(Event input) {
		if (input.isCharacter('v')) {
			_window.switchToVisualMode();
			return RecognitionState.Recognized;
		}
		return RecognitionState.NotRecognized;
	}

}
