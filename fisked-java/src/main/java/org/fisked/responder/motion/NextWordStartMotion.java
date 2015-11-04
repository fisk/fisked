package org.fisked.responder.motion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class NextWordStartMotion implements IMotion {
	
	private BufferWindow _window;
	
	public NextWordStartMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matchesExact(nextEvent, "w");
	}

	private Pattern _nextWordPattern = Pattern.compile("\\s([^\\s]+)");
	
	@Override
	public MotionRange getMotionRange() {
		Buffer buffer = _window.getBuffer();
		int index = buffer.getPointIndex();
		boolean found = false;
		
		CharSequence string = buffer.getCharSequence();
		Matcher matcher = _nextWordPattern.matcher(string);
		if (matcher.find(index)) {
			index = matcher.start(1);
			found = true;
		}
		
		return new MotionRange(buffer.getPointIndex(), found ? index : buffer.getPointIndex());
	}

}
