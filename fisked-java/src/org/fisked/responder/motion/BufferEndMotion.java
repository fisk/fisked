package org.fisked.responder.motion;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class BufferEndMotion implements IMotion {
	
	private BufferWindow _window;
	
	public BufferEndMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState handleInput(Event nextEvent) {
		return EventRecognition.matches(nextEvent, "G");
	}

	@Override
	public MotionRange getRange() {
		return new MotionRange(_window.getBuffer().getPointIndex(), _window.getBuffer().getLength());
	}

}
