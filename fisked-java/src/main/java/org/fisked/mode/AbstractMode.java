package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.IRecognitionAction;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.RecognitionState;
import org.fisked.services.ServiceManager;

public abstract class AbstractMode implements IInputResponder {
	protected BufferWindow _window;
	protected InputResponderChain _responders = new InputResponderChain();
	
	public AbstractMode(BufferWindow window) {
		_window = window;
	}
	
	abstract public String getModeName();
	
	protected void addResponder(IInputRecognizer responder) {
		_responders.addResponder(responder);
	}
	
	protected void addResponder(IInputResponder responder) {
		_responders.addResponder(responder);
	}
	
	protected void addResponder(IInputRecognizer responder, IRecognitionAction callback) {
		_responders.addResponder(responder, callback);
	}
	
	protected void addResponder(String match, IRecognitionAction callback) {
		_responders.addResponder(match, callback);
	}
	
	public abstract Face getModelineFace();
	
	public abstract void activate();

	protected final int CURSOR_BLOCK = 0;
	protected final int CURSOR_VERTICAL_BAR = 1;
	protected final int CURSOR_UNDERLINE = 2;
	
	protected void changeCursor(int cursor) {
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();
		cs.getCursorService().changeCursor(cursor);
	}
	
	@Override
	public RecognitionState recognizesInput(Event input) {
		return _responders.recognizesInput(input);
	}
	
	@Override
	public void onRecognize() {
		_responders.onRecognize();
	}
}
