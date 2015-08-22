package org.fisked.mode;

import org.fisked.Application;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.Window;

import jcurses.system.InputChar;

public class NormalMode extends AbstractMode {
	private Window _window;
	
	public NormalMode(Window window) {
		_window = window;
	}
	
	@Override
	public boolean handleInput(InputChar input) {
		if (input.getCharacter() == 'q') {
			Application.getApplication().exit();
		}
		Buffer buffer = _window.getBuffer();
		buffer.appendCharAtPoint(input.getCharacter());
		_window.draw();
		_window.drawPoint();
		return true;
	}
}
