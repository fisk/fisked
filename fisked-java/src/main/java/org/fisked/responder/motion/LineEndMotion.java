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
		return EventRecognition.matchesExact(nextEvent, "$");
	}

	@Override
	public MotionRange getMotionRange() {
		Buffer buffer = _window.getBuffer();
		CharSequence string = buffer.getCharSequence();

		int newIndex = buffer.getPointIndex();
		if (newIndex != string.length()) {
			if (String.valueOf(string.charAt(newIndex)).matches(".")) {
				newIndex++;
				while (newIndex < string.length() && String.valueOf(string.charAt(newIndex)).matches(".")) {
					newIndex++;
				}
			}
		}
		
		return new MotionRange(buffer.getPointIndex(), newIndex);
	}

}
