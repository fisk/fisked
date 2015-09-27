package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.util.InputController;
import org.fisked.mode.util.InputController.CommandControllerDelegate;
import org.fisked.responder.Event;

public class NormalMode extends AbstractMode implements CommandControllerDelegate {
	private InputController _commandController = new InputController();

	public NormalMode(BufferWindow window) {
		super(window);
	}

	@Override
	public boolean handleInput(Event input) {
		_commandController.handleInput(input, _window, this);
		return true;
	}

	@Override
	public String getModeName() {
		return "normal";
	}

	@Override
	public void handleCommand(Event input) {
		if (input.getCharacter() == 'i') {
			_window.switchToInputMode();
		} else if (input.getCharacter() == 'v') {
			_window.switchToVisualMode();
		}
	}

}
