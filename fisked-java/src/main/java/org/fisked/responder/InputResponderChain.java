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
package org.fisked.responder;

import java.util.ArrayList;
import java.util.List;

public class InputResponderChain implements IInputResponder {
	private final List<IInputResponder> _responders = new ArrayList<>();
	private IInputResponder _matched = null;
	private IInputResponder _parent;

	public InputResponderChain() {
	}

	public InputResponderChain(IInputResponder parent) {
		_parent = parent;
	}

	public IInputResponder getParent() {
		return _parent;
	}

	public void setParent(IInputResponder parent) {
		_parent = parent;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		_matched = null;
		boolean maybeRecognized = false;
		for (IInputResponder responder : _responders) {
			RecognitionState state = responder.recognizesInput(nextEvent);
			if (state == RecognitionState.MaybeRecognized)
				maybeRecognized = true;
			if (state == RecognitionState.Recognized) {
				_matched = responder;
				return RecognitionState.Recognized;
			}
		}
		RecognitionState status = maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
		if (_parent != null && status == RecognitionState.NotRecognized) {
			status = _parent.recognizesInput(nextEvent);
			if (status == RecognitionState.Recognized) {
				_matched = _parent;
			}
		}
		return status;
	}

	public void addResponder(IInputResponder recognizer) {
		_responders.add(recognizer);
	}

	public void addResponder(IInputRecognizer recognizer, IRecognitionAction callback) {
		_responders.add(EventRecognition.newResponder(recognizer, callback));
	}

	public void addResponder(String match, IRecognitionAction callback) {
		addResponder((Event event) -> {
			return EventRecognition.matchesExact(event, match);
		} , callback);
	}

	@Override
	public void onRecognize() {
		if (_matched != null) {
			_matched.onRecognize();
		}
	}

	public void addResponder(IInputRecognizer responder) {
		_responders.add(EventRecognition.newResponder(responder, () -> {
		}));
	}
}
