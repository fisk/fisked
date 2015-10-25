package org.fisked.responder;

import java.util.ArrayList;
import java.util.List;

public class InputResponderChain implements IInputResponder {
	private List<IInputResponder> _responders = new ArrayList<>();
	private IInputResponder _matched = null;

	public InputResponderChain() {}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		_matched = null;
		boolean maybeRecognized = false;
		for (IInputResponder responder : _responders) {
			RecognitionState state = responder.recognizesInput(nextEvent);
			if (state == RecognitionState.MaybeRecognized) maybeRecognized = true;
			if (state == RecognitionState.Recognized) {
				_matched = responder;
				return RecognitionState.Recognized;
			}
		}
		return maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}

	public void addResponder(IInputResponder recognizer) {
		_responders.add(recognizer);
	}

	public void addResponder(IInputRecognizer recognizer, IRecognitionAction callback) {
		_responders.add(EventRecognition.newResponder(recognizer, callback));
	}
	
	public void addResponder(String match, IRecognitionAction callback) {
		addResponder((Event event) -> {
			return EventRecognition.matches(event, match);
		}, callback);
	}

	public void onRecognize() {
		if (_matched != null) {
			_matched.onRecognize();
		}
	}

	public void addResponder(IInputRecognizer responder) {
		_responders.add(EventRecognition.newResponder(responder, () -> {}));
	}
}
