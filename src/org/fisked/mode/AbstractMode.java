package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.IRawInputResponder;

public abstract class AbstractMode implements IRawInputResponder {
	protected BufferWindow _window;
	
	public AbstractMode(BufferWindow window) {
		_window = window;
	}
	
	abstract public String getModeName();
}
