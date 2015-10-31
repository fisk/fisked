package org.fisked.responder;

import java.util.Iterator;

/**
 * Utility class for event recognition
 * @author fisk
 *
 */

public class EventRecognition {
	private RecognitionState _state;
	private Event _spill;
	
	private static EventRecognition matchesRecognition(Event event, String string) {
		EventRecognition recognition = new EventRecognition();
		Iterator<Event> eventIter = event.iterator();
		int index = 0;
		while (eventIter.hasNext() && index < string.length()) {
			Event subEvent = eventIter.next();
			char character = string.charAt(index);
			if (!subEvent.isCharacter(character)) {
				recognition._state = RecognitionState.NotRecognized;
				return recognition;
			}
			index++;
		}
		
		if (eventIter.hasNext()) {
			recognition._state = RecognitionState.Recognized;
			recognition._spill = eventIter.next();
			return recognition;
		}
		
		if (index < string.length()) {
			recognition._state = RecognitionState.MaybeRecognized;
			return recognition;
		}

		recognition._state = RecognitionState.Recognized;
		return recognition;
	}
	
	public static RecognitionState matchesJoined(Event event, String string, IInputRecognizer recognizer) {
		EventRecognition recognition = matchesRecognition(event, string);
		if (recognition._state == RecognitionState.Recognized) {
			if (recognition._spill == null) {
				return RecognitionState.MaybeRecognized;
			}
			return recognizer.recognizesInput(recognition._spill);
		}
		return recognition._state;
	}
	
	public static RecognitionState matchesExact(Event event, String string) {
		EventRecognition result = matchesRecognition(event, string);
		if (result._state == RecognitionState.Recognized && result._spill != null) return RecognitionState.NotRecognized;
		return result._state;
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
