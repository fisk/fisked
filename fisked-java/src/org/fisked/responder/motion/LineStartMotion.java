package org.fisked.responder.motion;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class LineStartMotion implements IMotion {

	private BufferWindow _window;

	public LineStartMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matches(nextEvent, "0");
	}

	@Override
	public MotionRange getRange() {
		Buffer buffer = _window.getBuffer();
		int newIndex = buffer.getPointIndex();
		if (newIndex != 0) {
			if (String.valueOf(buffer.getStringBuilder().charAt(newIndex - 1)).matches(".")) {

				newIndex--;
				while (newIndex >= 0 && String.valueOf(buffer.getStringBuilder().charAt(newIndex)).matches(".")) {
					newIndex--;
				}
				newIndex++;
			}
		}

		return new MotionRange(_window.getBuffer().getPointIndex(), newIndex);
	}

}
