package org.fisked.responder;

import jcurses.system.InputChar;
import jcurses.system.Toolkit;

public class EventLoop {
	private IRawInputResponder _primaryResponder;
	
	public void start() {
		while (true) {
			InputChar nextChar = Toolkit.readCharacter();
			_primaryResponder.handleInput(nextChar);
		}
	}
	
	public IRawInputResponder getPrimaryResponder() {
		return _primaryResponder;
	}
	
	public void setPrimaryResponder(IRawInputResponder _primaryResponder) {
		this._primaryResponder = _primaryResponder;
	}
}
