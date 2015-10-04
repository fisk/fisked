package org.fisked.responder;

import java.util.ArrayList;
import java.util.List;

public class InputResponderChain implements IInputResponder {
	private List<IInputResponder> _responders = new ArrayList<IInputResponder>();

	public interface OnRecognizeCallback {
		void onRecognize();
	}

	public InputResponderChain() {}

	@Override
	public boolean handleInput(Event nextEvent) {
		for (IInputResponder responder : _responders) {
			if (responder.handleInput(nextEvent)) return true;
		}
		return false;
	}

	public void addResponder(IInputResponder recognizer) {
		_responders.add(recognizer);
	}

	public void addResponder(IInputResponder recognizer, OnRecognizeCallback callback) {
		_responders.add((Event nextEvent) -> {
			boolean result = recognizer.handleInput(nextEvent);
			if (result) {
				callback.onRecognize();
			}
			return result;
		});
	}
}
