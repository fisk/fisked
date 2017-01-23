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
package org.fisked.responder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for event recognition
 *
 * @author fisk
 *
 */

public class EventRecognition {
	private RecognitionState _state;
	private Event _spill;

	public RecognitionState getState() {
		return _state;
	}

	public Event getSpillEvent() {
		return _spill;
	}

	public static EventRecognition matchesRecognition(Event event, String string) {
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

	public static EventRecognition matchesRecognition(Event event, IInputRecognizer recognizer) {
		EventRecognition recognition = new EventRecognition();
		int length = event.length();
		int tryLength = 1;
		do {
			Event subEvent = event.subevent(tryLength);
			if (subEvent == null) {
				recognition._state = RecognitionState.MaybeRecognized;
				return recognition;
			}
			RecognitionState state = recognizer.recognizesInput(subEvent);
			if (state == RecognitionState.NotRecognized) {
				recognition._state = state;
				return recognition;
			} else if (state == RecognitionState.Recognized) {
				recognition._state = state;
				recognition._spill = event.get(tryLength);
				return recognition;
			}
			tryLength++;
		} while (tryLength <= length);
		recognition._state = RecognitionState.MaybeRecognized;
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

	public static RecognitionState matchesJoinedOptionalPrefix(Event event, IInputRecognizer prefixRecognizer,
			IInputRecognizer requiredRecognizer) {
		EventRecognition recognition = matchesRecognition(event, prefixRecognizer);
		if (recognition._state == RecognitionState.Recognized) {
			if (recognition._spill == null) {
				return RecognitionState.MaybeRecognized;
			}
			return requiredRecognizer.recognizesInput(recognition._spill);
		} else if (recognition._state == RecognitionState.NotRecognized) {
			return requiredRecognizer.recognizesInput(event);
		}
		return RecognitionState.MaybeRecognized;
	}

	public static RecognitionState matchesExact(Event event, String string) {
		EventRecognition result = matchesRecognition(event, string);
		if (result._state == RecognitionState.Recognized && result._spill != null)
			return RecognitionState.NotRecognized;
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

	public static class EventRecognizerBuilder {
		private final List<IInputRecognizer> _recognizers = new ArrayList<>();

		private static class Optional implements IInputRecognizer {
			private final IInputRecognizer _internal;

			public Optional(IInputRecognizer internal) {
				_internal = internal;
			}

			@Override
			public RecognitionState recognizesInput(Event nextEvent) {
				return _internal.recognizesInput(nextEvent);
			}

		}

		private class MergedRecognizer implements IInputRecognizer {

			@Override
			public RecognitionState recognizesInput(Event nextEvent) {
				Event current = nextEvent;
				for (IInputRecognizer recognizer : _recognizers) {
					if (current == null) {
						return RecognitionState.MaybeRecognized;
					}
					EventRecognition state = matchesRecognition(current, recognizer);
					if (state._state == RecognitionState.Recognized) {
						current = state._spill;
						continue;
					} else if (state._state == RecognitionState.NotRecognized) {
						if (recognizer instanceof Optional) {
							continue;
						} else {
							return RecognitionState.NotRecognized;
						}
					} else {
						return RecognitionState.MaybeRecognized;
					}
				}
				if (current == null) {
					return RecognitionState.Recognized;
				} else {
					return RecognitionState.NotRecognized;
				}
			}

		}

		private class MergedResponder extends MergedRecognizer implements IInputResponder {
			private final IRecognitionAction _action;

			public MergedResponder(IRecognitionAction action) {
				_action = action;
			}

			@Override
			public void onRecognize() {
				_action.onRecognize();
			}
		}

		public EventRecognizerBuilder optional(IInputRecognizer recognizer) {
			_recognizers.add(new Optional(recognizer));
			return this;
		}

		public EventRecognizerBuilder require(IInputRecognizer recognizer) {
			_recognizers.add(recognizer);
			return this;
		}

		public IInputRecognizer build() {
			return new MergedRecognizer();
		}

		public IInputResponder build(IRecognitionAction action) {
			return new MergedResponder(action);
		}
	}

	public static EventRecognizerBuilder builder() {
		return new EventRecognizerBuilder();
	}

	private static class StringRecognizer implements IInputRecognizer {
		private final String _string;

		public StringRecognizer(String string) {
			_string = string;
		}

		@Override
		public RecognitionState recognizesInput(Event nextEvent) {
			return matchesExact(nextEvent, _string);
		}
	}

	public static IInputRecognizer recognizer(String str) {
		return new StringRecognizer(str);
	}
}
