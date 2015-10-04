package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.InputResponderChain.OnRecognizeCallback;

public abstract class AbstractMode implements IInputResponder {
	protected BufferWindow _window;
	protected InputResponderChain _responders = new InputResponderChain();
	
	public AbstractMode(BufferWindow window) {
		_window = window;
	}
	
	abstract public String getModeName();
	
	protected void addResponder(IInputResponder responder) {
		_responders.addResponder(responder);
	}
	
	protected void addResponder(IInputResponder responder, OnRecognizeCallback callback) {
		_responders.addResponder(responder, callback);
	}
	
	public abstract Face getModelineFace();
	
	@Override
	public boolean handleInput(Event input) {
		return _responders.handleInput(input);
	}
}
