package org.fisked.buffer;

import org.fisked.renderingengine.service.models.AttributedString;

public class ModeLineController {
	
	private BufferWindow _window;
	
	public ModeLineController(BufferWindow window) {
		_window = window;
	}

	public AttributedString getModeLineText() {
		StringBuilder str = new StringBuilder();
		str.append(_window.getCurrentMode().getModeName().toUpperCase());
		str.append("> ");
		
		return new AttributedString(str.toString(), _window.getCurrentMode().getModelineFace());
	}

}
