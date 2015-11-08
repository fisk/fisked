package org.fisked.mode.responder;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;

public class TextInputResponder implements IInputRecognizer {
	private final BufferWindow _window;
	private int _numActions;

	public TextInputResponder(BufferWindow window, int preactions) {
		_window = window;
		_numActions = preactions;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (input.isBackspace()) {
			_numActions++;
			_window.getBuffer().removeCharAtPointLogged();
			_window.setNeedsFullRedraw();
			return RecognitionState.Recognized;
		}
		Buffer buffer = _window.getBuffer();
		_numActions++;
		if (input.isReturn()) {
			buffer.appendCharAtPointLogged('\n');
		} else {
			buffer.appendCharAtPointLogged(input.getCharacter());
		}
		_window.setNeedsFullRedraw();
		return RecognitionState.Recognized;
	}

	public int getNumActions() {
		return _numActions;
	}

}
