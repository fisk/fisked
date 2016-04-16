package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.IRecognitionAction;
import org.fisked.responder.RecognitionState;

public class CommandInputResponder implements IInputResponder {
	private boolean _writingCommand = false;
	private final BufferWindow _window;
	private IRecognitionAction _action;

	public CommandInputResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (_writingCommand) {
			_action = () -> {
				if (_window.getCommandController().recognizesInput(input) == RecognitionState.NotRecognized) {
					_writingCommand = false;
				}
				_window.setNeedsFullRedraw();
			};
			return RecognitionState.Recognized;
		}
		if (input.getCharacter() == ':') {
			_action = () -> {
				_window.getCommandController().setCommandFeedback(null);
				_writingCommand = true;
				_window.getCommandController().startCommand();
				_window.setNeedsFullRedraw();
			};
			return RecognitionState.Recognized;
		} else {
			return RecognitionState.NotRecognized;
		}
	}

	@Override
	public void onRecognize() {
		IRecognitionAction action = _action;
		if (action != null) {
			action.onRecognize();
		}
	}

}
