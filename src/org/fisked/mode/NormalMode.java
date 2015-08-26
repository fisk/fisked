package org.fisked.mode;

import org.fisked.Application;
import org.fisked.buffer.BufferWindow;
import org.fisked.log.Log;
import org.fisked.responder.Event;

public class NormalMode extends AbstractMode {
	
	public NormalMode(BufferWindow window) {
		super(window);
	}
	
	@Override
	public boolean handleInput(Event input) {
		if (input.getCharacter() == 'q') {
			Application.getApplication().exit();
		} else if (input.getCharacter() == 'i') {
			_window.switchToInputMode();
		}
		if (input.isSpecialCode()) {
			Log.println("This is special: " + input.getCode());
		} else {
			Log.println("Not so special: " + input.getCode() + ", " + input.getCharacter());
		}
		return true;
	}

	@Override
	public String getModeName() {
		return "normal";
	}
	
}
