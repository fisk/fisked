package org.fisked.responder;

import org.fisked.log.Log;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.services.ServiceManager;

public class EventLoop {
	private IInputResponder _primaryResponder;
	
	public void start() {
		while (true) {
			ServiceManager sm = ServiceManager.getInstance();
			IConsoleService cs = sm.getConsoleService();
			int nextChar = cs.getChar();
			Log.println("Got event: " + nextChar);
			Event nextEvent = new Event(nextChar);
			_primaryResponder.handleInput(nextEvent);
		}
	}
	
	public IInputResponder getPrimaryResponder() {
		return _primaryResponder;
	}
	
	public void setPrimaryResponder(IInputResponder _primaryResponder) {
		this._primaryResponder = _primaryResponder;
	}
}
