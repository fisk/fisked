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
package org.fisked.mode.responder;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.traverse.IFilterVisitor;
import org.fisked.buffer.registers.RegisterManager;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.util.models.Range;
import org.fisked.util.models.selection.SelectionMode;
import org.fisked.util.models.selection.TextSelection;

public class DeleteLineResponder implements IInputResponder {

	private final BufferWindow _window;

	public DeleteLineResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matchesExact(nextEvent, "dd");
	}

	private int getLineStart(Cursor cursor) {
		Buffer buffer = _window.getBuffer();
		String string = buffer.getCharSequence().toString();

		int newIndex = cursor.getCharIndex();
		if (newIndex != 0) {
			if (String.valueOf(string.charAt(newIndex - 1)).matches(".")) {
				newIndex--;
				while (newIndex >= 0 && String.valueOf(string.charAt(newIndex)).matches(".")) {
					newIndex--;
				}
				newIndex++;
			}
		}

		return newIndex;
	}

	private int getLineEnd(Cursor cursor) {
		Buffer buffer = _window.getBuffer();
		String string = buffer.getCharSequence().toString();

		int newIndex = cursor.getCharIndex();
		if (newIndex != string.length()) {
			if (String.valueOf(string.charAt(newIndex)).matches(".")) {
				newIndex++;
				while (newIndex < string.length() && String.valueOf(string.charAt(newIndex)).matches(".")) {
					newIndex++;
				}
			}
		}

		return newIndex;
	}

	@Override
	public void onRecognize() {
		Buffer buffer = _window.getBuffer();

		try (UndoScope us = buffer.createUndoScope()) {
			IFilterVisitor<Cursor> visitor = new IFilterVisitor<Cursor>() {
				@Override
				public boolean visit(Cursor traversable) {
					int start = getLineStart(traversable);
					int lineEnd = getLineEnd(traversable) + 1;
					int end = Math.min(lineEnd, buffer.length());
					start -= lineEnd - end;
					start = Math.max(start, 0);
					Range lineRange = new Range(start, end - start);

					String string = buffer.toString().substring(lineRange.getStart(), lineRange.getEnd());
					RegisterManager.getInstance().setRegister(RegisterManager.UNNAMED_REGISTER,
							new TextSelection(SelectionMode.LINE_MODE, string));

					if (lineRange.getLength() != 0) {
						buffer.removeCharsInRangeLogged(lineRange);
					}

					return true;
				}
			};
			buffer.getCursorCollection().doFilteredReverse(visitor);
		}
		_window.setNeedsFullRedraw();
	}

}
