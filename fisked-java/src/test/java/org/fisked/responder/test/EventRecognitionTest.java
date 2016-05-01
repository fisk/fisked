package org.fisked.responder.test;

import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EventRecognitionTest {

	Event createEvent(String string) {
		if (string.length() == 1) {
			return new Event(string.charAt(0));
		} else {
			String postfix = string.substring(1, string.length());
			char first = string.charAt(0);

			Event postfixEvent = createEvent(postfix);
			Event firstEvent = new Event(first);

			firstEvent.setNext(postfixEvent);
			return firstEvent;
		}
	}

	private Event _testEvent;
	private Event _estEvent;
	private Event _teEvent;
	private Event _testtEvent;

	@BeforeClass
	void setUp() {
		_testEvent = createEvent("test");
		_estEvent = createEvent("est");
		_teEvent = createEvent("te");
		_testtEvent = createEvent("testt");
	}

	@Test
	void testEvent() {
		Assert.assertEquals("test", _testEvent.getString());
		Assert.assertEquals("tes", _testEvent.subevent(3).getString());
	}

	@Test
	void testTextRecognizer() {
		IInputRecognizer testRecognizer = EventRecognition.recognizer("test");
		IInputRecognizer tRecognizer = EventRecognition.recognizer("t");

		RecognitionState testtRecognizerState = EventRecognition.matchesJoinedOptionalPrefix(_testtEvent,
				testRecognizer, tRecognizer);

		Assert.assertTrue(EventRecognition.matchesExact(_testEvent, "test") == RecognitionState.Recognized);
		Assert.assertEquals(testRecognizer.recognizesInput(_testEvent), RecognitionState.Recognized);
		Assert.assertEquals(testRecognizer.recognizesInput(_teEvent), RecognitionState.MaybeRecognized);
		Assert.assertEquals(testRecognizer.recognizesInput(_testtEvent), RecognitionState.NotRecognized);
		Assert.assertEquals(testtRecognizerState, RecognitionState.Recognized);
	}

	@Test
	void testMergeRecognizer() {
		IInputRecognizer testRecognizer = EventRecognition.recognizer("test");
		IInputRecognizer tRecognizer = EventRecognition.recognizer("t");
		IInputRecognizer estRecognizer = EventRecognition.recognizer("est");
		IInputRecognizer tOptionalEstRecognizer = EventRecognition.builder().optional(tRecognizer)
				.require(estRecognizer).build();

		Assert.assertEquals(tOptionalEstRecognizer.recognizesInput(_testEvent), RecognitionState.Recognized);
		Assert.assertEquals(tOptionalEstRecognizer.recognizesInput(_estEvent), RecognitionState.Recognized);
		Assert.assertEquals(tOptionalEstRecognizer.recognizesInput(_teEvent), RecognitionState.MaybeRecognized);
		Assert.assertEquals(tOptionalEstRecognizer.recognizesInput(_testtEvent), RecognitionState.NotRecognized);

		Assert.assertEquals(testRecognizer.recognizesInput(_testEvent),
				tOptionalEstRecognizer.recognizesInput(_testEvent));
		Assert.assertEquals(testRecognizer.recognizesInput(_teEvent), tOptionalEstRecognizer.recognizesInput(_teEvent));
		Assert.assertEquals(testRecognizer.recognizesInput(_testtEvent),
				tOptionalEstRecognizer.recognizesInput(_testtEvent));
	}
}
