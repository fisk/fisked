package org.fisked.responder;

import java.util.ArrayList;
import java.util.List;

public class InputResponderChain implements IInputResponder {
	private List<IInputResponder> _responders = new ArrayList<IInputResponder>();
	
	public InputResponderChain() {}

	@Override
	public boolean handleInput(Event nextEvent) {
		for (IInputResponder responder : _responders) {
			if (responder.handleInput(nextEvent)) return true;
		}
		return false;
	}

	public void addRecognizer(IInputResponder recognizer) {
		_responders.add(recognizer);
	}
}
