package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.mode.responder.TextInputResponder;

public class InputMode extends AbstractMode {
	
	public InputMode(BufferWindow window) {
		super(window);
		addRecognizer(new NormalModeSwitchResponder(_window));
		addRecognizer(new TextInputResponder(_window));
	}

	@Override
	public String getModeName() {
		return "input";
	}

}
