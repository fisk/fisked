package org.fisked.buffer;

public class ModeLineController {
	
	private BufferWindow _window;
	
	public ModeLineController(BufferWindow window) {
		_window = window;
	}

	public String getModeLineText() {
		StringBuilder str = new StringBuilder();
		str.append(_window.getCurrentMode().getModeName().toUpperCase());
		str.append("> ");
		return str.toString();
	}

}
