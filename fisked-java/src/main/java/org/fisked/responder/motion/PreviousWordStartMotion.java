package org.fisked.responder.motion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class PreviousWordStartMotion implements IMotion {
	
	private BufferWindow _window;
	
	public PreviousWordStartMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matchesExact(nextEvent, "b");
	}

	Pattern _previousWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");
	
	@Override
	public MotionRange getMotionRange() {
		Buffer buffer = _window.getBuffer();

		int index = buffer.getPointIndex();
		boolean found = false;
		CharSequence string = buffer.getCharSequence();
		StringBuilder reverse = new StringBuilder(string).reverse();
		int length = string.length();
		Matcher matcher = _previousWordPattern.matcher(reverse);
		if (matcher.find(length - index)) {
			int newIndex = matcher.end(1);
			found = true;
			index = length - newIndex;
		}
		
		return new MotionRange(buffer.getPointIndex(), found ? index : buffer.getPointIndex());
	}

}