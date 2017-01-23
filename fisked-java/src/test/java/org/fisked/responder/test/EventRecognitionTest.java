/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.responder.test;

import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class EventRecognitionTest {

	static Event createEvent(String string) {
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

	private static Event _testEvent;
	private static Event _estEvent;
	private static Event _teEvent;
	private static Event _testtEvent;

	@BeforeClass
	public static void setUp() {
		_testEvent = createEvent("test");
		_estEvent = createEvent("est");
		_teEvent = createEvent("te");
		_testtEvent = createEvent("testt");
	}

	@Test
	public void testEvent() {
		Assert.assertEquals("test", _testEvent.getString());
		Assert.assertEquals("tes", _testEvent.subevent(3).getString());
	}

	@Test
	public void testTextRecognizer() {
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
	public void testMergeRecognizer() {
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
