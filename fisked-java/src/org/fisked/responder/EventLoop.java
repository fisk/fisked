package org.fisked.responder;

import jcurses.system.InputChar;
import jcurses.system.Toolkit;

public class EventLoop {
	private IRawInputResponder _primaryResponder;
	
	public void start() {
		while (true) {
			InputChar nextChar = Toolkit.readCharacter();
			Event nextEvent = new Event(nextChar);
			_primaryResponder.handleInput(nextEvent);
		}
	}
	
	public IRawInputResponder getPrimaryResponder() {
		return _primaryResponder;
	}
	
	public void setPrimaryResponder(IRawInputResponder _primaryResponder) {
		this._primaryResponder = _primaryResponder;
	}
}
