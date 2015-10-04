package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.mode.responder.TextInputResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;

public class InputMode extends AbstractMode {
	
	public InputMode(BufferWindow window) {
		super(window);
		addResponder(new NormalModeSwitchResponder(_window));
		addResponder(new TextInputResponder(_window));
	}
	
	public Face getModelineFace() {
		return new Face(Color.RED, Color.BLACK);
	}

	@Override
	public String getModeName() {
		return "input";
	}

}
