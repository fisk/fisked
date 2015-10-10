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
	public RecognitionState handleInput(Event nextEvent) {
		boolean maybeRecognized = false;
		for (IInputResponder responder : _responders) {
			RecognitionState state = responder.handleInput(nextEvent);
			if (state == RecognitionState.MaybeRecognized) maybeRecognized = true;
			if (state == RecognitionState.Recognized) return RecognitionState.Recognized;
		}
		return maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}

	public void addResponder(IInputResponder recognizer) {
		_responders.add(recognizer);
	}

	public void addResponder(IInputResponder recognizer, OnRecognizeCallback callback) {
		_responders.add((Event nextEvent) -> {
			RecognitionState result = recognizer.handleInput(nextEvent);
			if (result == RecognitionState.Recognized) {
				callback.onRecognize();
			}
			return result;
		});
	}
	
	public interface IRecognizeCallback {
		void callback();
	}
	
	public void addResponder(String match, OnRecognizeCallback callback) {
		addResponder((Event event) -> {
			return EventRecognition.matches(event, match);
		}, callback);
	}
}
