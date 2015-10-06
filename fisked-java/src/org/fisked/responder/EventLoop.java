package org.fisked.responder;

import org.fisked.buffer.drawing.Window;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.services.ServiceManager;

public class EventLoop {
	private Window _primaryResponder;
	
	public void start() {
		while (true) {
			ServiceManager sm = ServiceManager.getInstance();
			IConsoleService cs = sm.getConsoleService();
			int nextChar = cs.getChar();
			Event nextEvent = new Event(nextChar);
			_primaryResponder.handleInput(nextEvent);
			_primaryResponder.refresh();
		}
	}
	
	public IInputResponder getPrimaryResponder() {
		return _primaryResponder;
	}
	
	public void setPrimaryResponder(Window _primaryResponder) {
		this._primaryResponder = _primaryResponder;
	}
}
