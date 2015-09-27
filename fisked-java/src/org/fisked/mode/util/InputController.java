package org.fisked.mode.util;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;

public class InputController {
	boolean _writingCommand = false;
	
	public interface CommandControllerDelegate {
		void handleCommand(Event input);
	}

	public void handleInput(Event input, BufferWindow window, CommandControllerDelegate delegate) {
		try {
			if (_writingCommand) {
				if (!window.getCommandController().handleInput(input)) {
					_writingCommand = false;
					return;
				}
			}
			window.getCommandController().setCommandFeedback(null);
			if (input.getCharacter() == ':') {
				_writingCommand = true;
				window.getCommandController().startCommand();
			} else {
				delegate.handleCommand(input);
			}
		} finally {
			window.refresh();
		}
	}

}
