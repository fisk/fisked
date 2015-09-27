package org.fisked.mode.responder;

import java.util.ArrayList;
import java.util.List;

import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;

public class InputResponderChain implements IRawInputResponder {
	private List<IRawInputResponder> _responders = new ArrayList<IRawInputResponder>();
	
	public InputResponderChain() {}

	@Override
	public boolean handleInput(Event nextEvent) {
		for (IRawInputResponder responder : _responders) {
			if (responder.handleInput(nextEvent)) return true;
		}
		return false;
	}

	public void addRecognizer(IRawInputResponder recognizer) {
		_responders.add(recognizer);
	}
}
