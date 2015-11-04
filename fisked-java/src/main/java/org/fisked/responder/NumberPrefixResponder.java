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
