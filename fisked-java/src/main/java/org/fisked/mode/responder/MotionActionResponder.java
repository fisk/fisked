package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.LoopResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.responder.motion.IMotion.MotionRange;
import org.fisked.responder.motion.MotionRecognizer;

public class MotionActionResponder implements IInputResponder {
	
	private final BufferWindow _window;
	private final InputResponderChain _responders = new InputResponderChain();
	private final LoopResponder _numberPrefix = new LoopResponder(_responders);
	
	public MotionActionResponder(BufferWindow window) {
		_window = window;
		final MotionRecognizer motionRecognizer = new MotionRecognizer(window);

		_responders.addResponder((Event event) -> {
			return EventRecognition.matchesJoined(event, "d", motionRecognizer);
		}, () -> {
			MotionRange range = motionRecognizer.getMotionRange();
			_window.getBuffer().removeCharsInRange(range.getRange());
		});
		_responders.addResponder((Event event) -> {
			return EventRecognition.matchesJoined(event, "c", motionRecognizer);
		}, () -> {
			MotionRange range = motionRecognizer.getMotionRange();
			_window.getBuffer().removeCharsInRange(range.getRange());
			_window.switchToInputMode();
		});
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return _numberPrefix.recognizesInput(nextEvent);
	}

	@Override
	public void onRecognize() {
		_numberPrefix.onRecognize();
	}

}
