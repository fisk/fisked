/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
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
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;

public class FindMotion implements IMotion {
	private final BufferWindow _window;
	private char _character;
	private boolean _forward;

	public FindMotion(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		int i = 0;
		for (Event event : nextEvent) {
			if (!event.isCharacter())
				return RecognitionState.NotRecognized;
			if (i == 0) {
				if (event.isCharacter('f')) {
					_forward = true;
				} else if (event.isCharacter('F')) {
					_forward = false;
				} else {
					return RecognitionState.NotRecognized;
				}
			} else if (i == 1) {
				_character = event.getCharacter();
			} else {
				if (!event.isCharacter())
					return RecognitionState.NotRecognized;
			}
			i++;
		}
		if (i < 2)
			return RecognitionState.MaybeRecognized;
		return RecognitionState.Recognized;
	}

	@Override
	public MotionRange getMotionRange(Cursor cursor) {
		Buffer buffer = _window.getBuffer();
		int endIndex;
		int startIndex = cursor.getCharIndex();
		if (_forward)
			startIndex++;
		else
			startIndex--;
		String string = buffer.getCharSequence().toString();
		int length = string.length();
		if (startIndex < 0)
			startIndex = 0;
		if (startIndex >= string.length())
			startIndex = length - 1;

		if (_forward) {
			int indexOf = string.indexOf(Character.toString(_character), startIndex);
			if (indexOf == -1) {
				endIndex = startIndex;
			} else {
				endIndex = indexOf;
			}
		} else {
			int indexOf = new StringBuilder(string).reverse().indexOf(Character.toString(_character),
					length - startIndex);
			if (indexOf == -1) {
				endIndex = startIndex;
			} else {
				endIndex = length - 1 - indexOf;
			}
		}
		return new MotionRange(startIndex, endIndex);
	}

}
