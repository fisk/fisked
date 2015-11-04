package org.fisked.responder.motion;

import org.apache.logging.log4j.LogManager;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.NumberPrefixResponder;
import org.fisked.responder.NumberPrefixResponder.INumberInputResponder;
import org.fisked.responder.RecognitionState;

public class GoToLineMotion implements IMotion {
	private final BufferWindow _window;
	private NumberPrefixResponder _prefix;
	private int _number;

	public GoToLineMotion(BufferWindow window) {
		_window = window;

		_prefix = new NumberPrefixResponder(new INumberInputResponder() {
			@Override
			public RecognitionState recognizesInput(Event nextEvent) {
				return EventRecognition.matchesExact(nextEvent, "G");
			}

			@Override
			public void onRecognize() {
				throw new IllegalStateException("This motion needs a number");
			}

			@Override
			public void onRecognize(int number) {
				_number = number;
			}
		});
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		RecognitionState state = _prefix.recognizesInput(nextEvent);
		if (state == RecognitionState.Recognized) {
			if (_prefix.hasNumber()) {
				_prefix.onRecognize();
			} else {
				state = RecognitionState.NotRecognized;
			}
		}
		return state;
	}

	@Override
	public MotionRange getMotionRange() {
		Buffer buffer = _window.getBuffer();
		int index = buffer.getTextLayout().getCharIndexForPhysicalLine(_number);
		LogManager.getLogger(GoToLineMotion.class).debug("Go to line: " + _number + ", index: " + index);

		return new MotionRange(buffer.getPointIndex(), index);
	}

}
