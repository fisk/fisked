package org.fisked.responder;

import java.util.ArrayList;
import java.util.List;

public class InputResponderChain implements IInputResponder {
	private final List<IInputResponder> _responders = new ArrayList<>();
	private IInputResponder _matched = null;
	private IInputResponder _parent;

	public InputResponderChain() {
	}

	public InputResponderChain(IInputResponder parent) {
		_parent = parent;
	}

	public IInputResponder getParent() {
		return _parent;
	}

	public void setParent(IInputResponder parent) {
		_parent = parent;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		_matched = null;
		boolean maybeRecognized = false;
		for (IInputResponder responder : _responders) {
			RecognitionState state = responder.recognizesInput(nextEvent);
			if (state == RecognitionState.MaybeRecognized)
				maybeRecognized = true;
			if (state == RecognitionState.Recognized) {
				_matched = responder;
				return RecognitionState.Recognized;
			}
		}
		RecognitionState status = maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
		if (_parent != null && status == RecognitionState.NotRecognized) {
			status = _parent.recognizesInput(nextEvent);
			if (status == RecognitionState.Recognized) {
				_matched = _parent;
			}
		}
		return status;
	}

	public void addResponder(IInputResponder recognizer) {
		_responders.add(recognizer);
	}

	public void addResponder(IInputRecognizer recognizer, IRecognitionAction callback) {
		_responders.add(EventRecognition.newResponder(recognizer, callback));
	}

	public void addResponder(String match, IRecognitionAction callback) {
		addResponder((Event event) -> {
			return EventRecognition.matchesExact(event, match);
		} , callback);
	}

	@Override
	public void onRecognize() {
		if (_matched != null) {
			_matched.onRecognize();
		}
	}

	public void addResponder(IInputRecognizer responder) {
		_responders.add(EventRecognition.newResponder(responder, () -> {
		}));
	}
}
