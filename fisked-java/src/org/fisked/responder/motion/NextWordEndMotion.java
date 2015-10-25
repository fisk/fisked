package org.fisked.responder.motion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class NextWordEndMotion implements IMotion {
	
	private BufferWindow _window;
	
	public NextWordEndMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matches(nextEvent, "e");
	}

	private Pattern _endOfWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");
	
	@Override
	public MotionRange getRange() {
		Buffer buffer = _window.getBuffer();
		int index = buffer.getPointIndex() + 1;
		boolean found = false;
		CharSequence string = buffer.getStringBuilder();
		if (index < string.length()) {
			Matcher matcher = _endOfWordPattern.matcher(string);
			if (matcher.find(index)) {
				int newIndex = matcher.end(1) - 1;
				if (newIndex > 0) {
					found = true;
					index = newIndex;
				}
			}
		}
		
		return new MotionRange(buffer.getPointIndex(), found ? index : buffer.getPointIndex());
	}

}
