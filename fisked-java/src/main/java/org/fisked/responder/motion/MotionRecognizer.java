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

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.cursor.Cursor;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;
import org.fisked.ui.buffer.BufferWindow;

public class MotionRecognizer implements IMotion {
	private final List<IMotion> _motions = new ArrayList<IMotion>();
	private IMotion _match = null;

	public MotionRecognizer(BufferWindow window) {
		_motions.add(new FindMotion(window));
		_motions.add(new BufferStartMotion(window));
		_motions.add(new BufferEndMotion(window));
		_motions.add(new GoToLineMotion(window));
		_motions.add(new LineStartMotion(window));
		_motions.add(new LineEndMotion(window));
		_motions.add(new NextWordStartMotion(window));
		_motions.add(new NextWordEndMotion(window));
		_motions.add(new PreviousWordStartMotion(window));
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		boolean maybe = false;
		for (IMotion motion : _motions) {
			RecognitionState state = motion.recognizesInput(nextEvent);
			if (state == RecognitionState.Recognized) {
				_match = motion;
				return state;
			} else if (state == RecognitionState.MaybeRecognized) {
				maybe = true;
			}
		}
		return maybe ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}

	@Override
	public MotionRange getMotionRange(Cursor cursor) {
		MotionRange range = _match.getMotionRange(cursor);
		return range;
	}

}
