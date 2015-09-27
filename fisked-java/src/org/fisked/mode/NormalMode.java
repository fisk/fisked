package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.InputModeSwitchResponder;
import org.fisked.mode.responder.VisualModeSwitchResponder;

public class NormalMode extends AbstractMode {

	public NormalMode(BufferWindow window) {
		super(window);
		addRecognizer(new CommandInputResponder(_window));
		addRecognizer(new InputModeSwitchResponder(_window));
		addRecognizer(new VisualModeSwitchResponder(_window));
		addRecognizer(new BasicNavigationResponder(_window));
	}

	@Override
	public String getModeName() {
		return "normal";
	}

}
