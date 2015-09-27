package org.fisked.mode;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.log.Log;
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
			} else if (input.getCharacter() == 'h') {
				moveLeft();
			} else if (input.getCharacter() == 'l') {
				moveRight();
			} else if (input.getCharacter() == 'j') {
				moveDown();
			} else if (input.getCharacter() == 'k') {
				moveUp();
			}
			return true;
		} finally {
			_window.refresh();
		}
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

}
