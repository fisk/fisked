package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;
import org.fisked.responder.InputResponderChain;

public abstract class AbstractMode implements IRawInputResponder {
	protected BufferWindow _window;
	protected InputResponderChain _recognizers = new InputResponderChain();
	
	public AbstractMode(BufferWindow window) {
		_window = window;
	}
	
	abstract public String getModeName();
	
	protected void addRecognizer(IRawInputResponder recognizer) {
		_recognizers.addRecognizer(recognizer);
	}
	
	@Override
	public boolean handleInput(Event input) {
		return _recognizers.handleInput(input);
	}
}
