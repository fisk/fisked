package org.fisked.responder.motion;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class LineEndMotion implements IMotion {
	
	private BufferWindow _window;
	
	public LineEndMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matches(nextEvent, "$");
	}

	@Override
	public MotionRange getRange() {
		Buffer buffer = _window.getBuffer();

		int newIndex = buffer.getPointIndex();
		if (newIndex != buffer.getStringBuilder().length()) {
			if (String.valueOf(buffer.getStringBuilder().charAt(newIndex)).matches(".")) {
				newIndex++;
				while (newIndex < buffer.getStringBuilder().length() && String.valueOf(buffer.getStringBuilder().charAt(newIndex)).matches(".")) {
					newIndex++;
				}
			}
		}
		
		return new MotionRange(buffer.getPointIndex(), newIndex);
	}

}
