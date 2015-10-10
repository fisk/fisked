package org.fisked.responder;

import org.fisked.buffer.drawing.Window;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.services.ServiceManager;

public class EventLoop {
	private Window _primaryResponder;

	public void start() {
		Event postponedStart = null;
		Event postponedEnd = null;

		while (true) {
			ServiceManager sm = ServiceManager.getInstance();
			IConsoleService cs = sm.getConsoleService();
			int nextChar = cs.getChar();
			Event nextEvent = new Event(nextChar);
			if (postponedEnd != null) {
				postponedEnd.setNext(nextEvent);
				postponedEnd = nextEvent;
			} else {
				postponedStart = postponedEnd = nextEvent;
			}
			RecognitionState state = _primaryResponder.handleInput(postponedStart);
			switch (state) {
			case Recognized:
			case NotRecognized:
				postponedStart = postponedEnd = null;
				break;

			case MaybeRecognized:
				break;
			}
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
