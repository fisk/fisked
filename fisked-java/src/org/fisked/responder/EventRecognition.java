package org.fisked.responder;

import java.util.Iterator;

/**
 * Utility class for event recognition
 * @author fisk
 *
 */

public class EventRecognition {
	public static RecognitionState matches(Event event, String string) {
		Iterator<Event> eventIter = event.iterator();
		int index = 0;
		while (eventIter.hasNext() && index < string.length()) {
			Event subEvent = eventIter.next();
			char character = string.charAt(index);
			if (!subEvent.isCharacter(character)) {
				return RecognitionState.NotRecognized;
			}
			index++;
		}
		
		if (eventIter.hasNext()) {
			return RecognitionState.NotRecognized;
		}
		
		if (index < string.length()) {
			return RecognitionState.MaybeRecognized;
		}

		return RecognitionState.Recognized;
	}
	
	public static IInputResponder newResponder(IInputRecognizer recognizer, IRecognitionAction action) {
		return new IInputResponder() {
			@Override
			public RecognitionState recognizesInput(Event nextEvent) {
				return recognizer.recognizesInput(nextEvent);
			}

			@Override
			public void onRecognize() {
				action.onRecognize();
			}
		};
	}
}
