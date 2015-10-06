package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.InputModeSwitchResponder;
import org.fisked.mode.responder.VisualModeSwitchResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;

public class NormalMode extends AbstractMode {

	public NormalMode(BufferWindow window) {
		super(window);
		addResponder(new CommandInputResponder(_window));
		addResponder(new InputModeSwitchResponder(_window));
		addResponder(new VisualModeSwitchResponder(_window));
		addResponder(new BasicNavigationResponder(_window));
	}
	
	public Face getModelineFace() {
		return new Face(Color.MAGENTA, Color.WHITE);
	}
	
	public void activate() {
		changeCursor(CURSOR_UNDERLINE);
	}

	@Override
	public String getModeName() {
		return "normal";
	}

}
