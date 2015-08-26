package org.fisked.buffer;

public class ModeLineController {
	
	private BufferWindow _window;
	
	public ModeLineController(BufferWindow window) {
		_window = window;
	}

	public String getModeLineText() {
		return "--" + _window.getCurrentMode().getModeName().toUpperCase() + "--";
	}

}
