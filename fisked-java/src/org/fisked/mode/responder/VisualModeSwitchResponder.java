package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;

public class VisualModeSwitchResponder implements IInputRecognizer {
	private final BufferWindow _window;
	
	public VisualModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (input.isCharacter('v')) {
			_window.switchToVisualMode();
			return RecognitionState.Recognized;
		}
		return RecognitionState.NotRecognized;
	}

}
