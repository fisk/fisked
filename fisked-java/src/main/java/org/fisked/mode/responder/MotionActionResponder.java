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
package org.fisked.mode.responder;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.controller.TextTransaction;
import org.fisked.buffer.cursor.TwinCursor;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.LoopResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.responder.motion.IMotion.MotionRange;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.responder.motion.MotionRecognizer;
import org.fisked.util.models.Range;

public class MotionActionResponder implements IInputResponder {

	private final BufferWindow _window;
	private final InputResponderChain _responders = new InputResponderChain();
	private final LoopResponder _numberPrefix = new LoopResponder(_responders);

	public MotionActionResponder(BufferWindow window) {
		_window = window;
		final MotionRecognizer motionRecognizer = new MotionRecognizer(window);

		_responders.addResponder((Event event) -> {
			return EventRecognition.matchesJoined(event, "d", motionRecognizer);
		}, () -> {
			TextTransaction transaction = _window.getBuffer().makeTextTransaction(true);
			transaction.executeDelete((TwinCursor twinCursor) -> {
				MotionRange motionRange = motionRecognizer.getMotionRange(twinCursor.getPrimary());
				List<Range> ranges = new ArrayList<>();
				ranges.add(motionRange.getRange());
				return ranges;
			});
			_window.setNeedsFullRedraw();
		});
		_responders.addResponder((Event event) -> {
			return EventRecognition.matchesJoined(event, "c", motionRecognizer);
		}, () -> {
			TextTransaction transaction = _window.getBuffer().makeTextTransaction(true);
			transaction.executeDelete((TwinCursor twinCursor) -> {
				MotionRange motionRange = motionRecognizer.getMotionRange(twinCursor.getPrimary());
				List<Range> ranges = new ArrayList<>();
				ranges.add(motionRange.getRange());
				return ranges;
			});
			_window.switchToInputMode();
		});
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return _numberPrefix.recognizesInput(nextEvent);
	}

	@Override
	public void onRecognize() {
		_numberPrefix.onRecognize();
	}

}
