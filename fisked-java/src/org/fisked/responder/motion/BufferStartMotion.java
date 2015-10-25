package org.fisked.responder.motion;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class BufferStartMotion implements IMotion {
	private BufferWindow _window;
	
	public BufferStartMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matches(nextEvent, "gg");
	}

	@Override
	public MotionRange getRange() {
		return new MotionRange(_window.getBuffer().getPointIndex(), 0);
	}

}
