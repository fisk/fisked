package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.mode.responder.TextInputResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;

public class InputMode extends AbstractMode {

	private final TextInputResponder _textInputResponder;

	public InputMode(BufferWindow window, int preactions) {
		super(window);
		addResponder(new NormalModeSwitchResponder(_window));
		_textInputResponder = new TextInputResponder(_window, preactions);
		addResponder(_textInputResponder);
	}

	@Override
	public Face getModelineFace() {
		return new Face(Color.RED, Color.WHITE);
	}

	@Override
	public void activate() {
		changeCursor(CURSOR_VERTICAL_BAR);
	}

	@Override
	public void deactivate() {
		getWindow().getBuffer().getUndoLog().merge(_textInputResponder.getNumActions());
	}

	@Override
	public String getModeName() {
		return "insert";
	}

}
