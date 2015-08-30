package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;

public class NormalMode extends AbstractMode {

	private boolean _writingCommand;

	public NormalMode(BufferWindow window) {
		super(window);
		_writingCommand = false;
	}

	@Override
	public boolean handleInput(Event input) {
		try {
			if (_writingCommand) {
				if (!_window.getCommandController().handleInput(input)) {
					_writingCommand = false;
				}
				return true;
			}
			_window.getCommandController().setCommandFeedback(null);
			if (input.getCharacter() == 'i') {
				_window.switchToInputMode();
			} else if (input.getCharacter() == ':') {
				_writingCommand = true;
				_window.getCommandController().startCommand();
			}
			return true;
		} finally {
			_window.refresh();
		}
	}

	@Override
	public String getModeName() {
		return "normal";
	}

}
