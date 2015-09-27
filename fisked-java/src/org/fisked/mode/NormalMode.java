package org.fisked.mode;

import org.fisked.buffer.Buffer;
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
	
	private void moveLeft() {
		Buffer buff = _window.getBuffer();
		buff.setPointIndex(buff.getPointIndex() - 1);
	}
	
	private void moveRight() {
		Buffer buff = _window.getBuffer();
		buff.setPointIndex(buff.getPointIndex() + 1);
	}
	
	private void moveDown() {
		Buffer buff = _window.getBuffer();
		int pos = buff.getPointIndex();
		int offset = 0;
		while (pos >= 0) {
			if (buff.getStringBuilder().charAt(pos) == '\n') {
				offset = buff.getPointIndex() - pos;
				break;
			}
			pos--;
		}
		if (pos == -1) { offset = buff.getPointIndex() + 1; }
		int lineEndsAt = buff.getStringBuilder().indexOf("\n", buff.getPointIndex());
		if (lineEndsAt != -1) {
			buff.setPointIndex(lineEndsAt + offset);
		}
	}
	
	private void moveUp() {
		Buffer buff = _window.getBuffer();
		int pos = buff.getPointIndex();
		int offset = 0;
		boolean foundFirstLinebreak = false;
		while (pos >= 0) {
			if (buff.getStringBuilder().charAt(pos) == '\n') {
				if (!foundFirstLinebreak) {
					offset = buff.getPointIndex() - pos;
					foundFirstLinebreak = true;
				} else {
					break;
				}
			}
			pos--;
		}

		buff.setPointIndex(pos + offset);
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
		} else if (input.getCharacter() == 'h') {
			moveLeft();
		} else if (input.getCharacter() == 'l') {
			moveRight();
		} else if (input.getCharacter() == 'j') {
			moveDown();
		} else if (input.getCharacter() == 'k') {
			moveUp();
		}
	}

}
