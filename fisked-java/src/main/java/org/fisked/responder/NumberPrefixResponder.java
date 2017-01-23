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

public class NumberPrefixResponder implements IInputResponder {
	private int _number;
	private boolean _hasNumber;
	private final INumberInputResponder _responder;

	public interface INumberInputResponder extends IInputResponder {
		void onRecognize(int number);
	}

	public NumberPrefixResponder(INumberInputResponder responder) {
		_responder = responder;
	}

	public boolean hasNumber() {
		return _hasNumber;
	}

	public int getNumber() {
		if (_hasNumber) {
			return _number;
		} else {
			return 1;
		}
	}

	private void parseNumber(ArrayList<Integer> list) {
		if (list == null) {
			_hasNumber = false;
			return;
		}
		int number = 0;
		final int listSize = list.size();
		int exp = 1;
		for (int i = 0; i < listSize; i++) {
			number += list.get(listSize - i - 1) * exp;
			exp *= 10;
		}
		_number = number;
		_hasNumber = true;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		if (_responder.recognizesInput(nextEvent) == RecognitionState.Recognized) {
			_hasNumber = false;
			return RecognitionState.Recognized;
		}
		ArrayList<Integer> integers = null;
		for (Event event : nextEvent) {
			if (!event.isCharacter()) {
				parseNumber(integers);
				return _responder.recognizesInput(event);
			}
			char character = event.getCharacter();
			if (!Character.isDigit(character)) {
				parseNumber(integers);
				return _responder.recognizesInput(event);
			}
			int num = character - '0';
			if (integers == null) {
				integers = new ArrayList<>();
			}
			integers.add(num);
		}
		_hasNumber = false;
		return RecognitionState.MaybeRecognized;
	}

	@Override
	public void onRecognize() {
		if (_hasNumber) {
			_responder.onRecognize(_number);
		} else {
			_responder.onRecognize();
		}
	}

}
