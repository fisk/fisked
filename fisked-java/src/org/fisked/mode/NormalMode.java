package org.fisked.mode;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.InputModeSwitchResponder;
import org.fisked.mode.responder.VisualModeSwitchResponder;
import org.fisked.responder.Event;

public class NormalMode extends AbstractMode {

	public NormalMode(BufferWindow window) {
		super(window);
		addRecognizer(new CommandInputResponder(_window));
		addRecognizer(new InputModeSwitchResponder(_window));
		addRecognizer(new VisualModeSwitchResponder(_window));
	}

	@Override
	public boolean handleInput(Event input) {
		if (input.getCharacter() == 'h') {
			moveLeft();
		} else if (input.getCharacter() == 'l') {
			moveRight();
		} else if (input.getCharacter() == 'j') {
			moveDown();
		} else if (input.getCharacter() == 'k') {
			moveUp();
		} else {
			return super.handleInput(input);
		}
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

}
