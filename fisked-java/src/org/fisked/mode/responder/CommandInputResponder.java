package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;

public class CommandInputResponder implements IInputResponder {
	private boolean _writingCommand = false;
	private BufferWindow _window;
	
	public CommandInputResponder(BufferWindow window) {
		_window = window;
	}

	public boolean handleInput(Event input) {
		if (_writingCommand) {
			if (!_window.getCommandController().handleInput(input)) {
				_writingCommand = false;
			}
			return true;
		}
		_window.getCommandController().setCommandFeedback(null);
		if (input.getCharacter() == ':') {
			_writingCommand = true;
			_window.getCommandController().startCommand();
			return true;
		} else {
			return false;
		}
	}

}
