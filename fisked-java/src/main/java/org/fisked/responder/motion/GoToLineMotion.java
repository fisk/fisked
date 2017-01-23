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
package org.fisked.responder.motion;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.NumberPrefixResponder;
import org.fisked.responder.NumberPrefixResponder.INumberInputResponder;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.responder.RecognitionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoToLineMotion implements IMotion {
	private final BufferWindow _window;
	private NumberPrefixResponder _prefix;
	private int _number;
	private static final Logger LOG = LoggerFactory.getLogger(GoToLineMotion.class);

	public GoToLineMotion(BufferWindow window) {
		_window = window;

		_prefix = new NumberPrefixResponder(new INumberInputResponder() {
			@Override
			public RecognitionState recognizesInput(Event nextEvent) {
				return EventRecognition.matchesExact(nextEvent, "G");
			}

			@Override
			public void onRecognize() {
				throw new IllegalStateException("This motion needs a number");
			}

			@Override
			public void onRecognize(int number) {
				_number = number;
			}
		});
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		RecognitionState state = _prefix.recognizesInput(nextEvent);
		if (state == RecognitionState.Recognized) {
			if (_prefix.hasNumber()) {
				_prefix.onRecognize();
			} else {
				state = RecognitionState.NotRecognized;
			}
		}
		return state;
	}

	@Override
	public MotionRange getMotionRange(Cursor cursor) {
		Buffer buffer = _window.getBuffer();
		int index = buffer.getTextLayout().getCharIndexForPhysicalLine(_number);
		LOG.debug("Go to line: " + _number + ", index: " + index);

		return new MotionRange(cursor.getCharIndex(), index);
	}

}
