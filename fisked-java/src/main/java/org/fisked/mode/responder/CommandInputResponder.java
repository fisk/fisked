package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;

public class CommandInputResponder implements IInputRecognizer {
	private boolean _writingCommand = false;
	private final BufferWindow _window;

	public CommandInputResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (_writingCommand) {
			if (_window.getCommandController().recognizesInput(input) == RecognitionState.NotRecognized) {
				_writingCommand = false;
			}
			_window.setNeedsFullRedraw();
			return RecognitionState.Recognized;
		}
		_window.getCommandController().setCommandFeedback(null);
		if (input.getCharacter() == ':') {
			_writingCommand = true;
			_window.getCommandController().startCommand();
			_window.setNeedsFullRedraw();
			return RecognitionState.Recognized;
		} else {
			return RecognitionState.NotRecognized;
		}
	}

}
