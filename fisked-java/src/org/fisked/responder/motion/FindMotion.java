package org.fisked.responder.motion;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;

public class FindMotion implements IMotion {
	private BufferWindow _window;
	private char _character;
	private int _startIndex;
	private int _endIndex;
	private boolean _forward;

	public FindMotion(BufferWindow window) {
		_window = window;
	}

	private void findIndex() {
		Buffer buffer = _window.getBuffer();
		_startIndex = buffer.getPointIndex();
		int startIndex = _startIndex;
		if (_forward) startIndex++;
		else startIndex--;
		StringBuilder string = buffer.getStringBuilder();
		int length = string.length();
		if (startIndex < 0) startIndex = 0;
		if (startIndex >= string.length()) startIndex = length - 1;
		
		if (_forward) {
			int indexOf = string.indexOf(Character.toString(_character), startIndex);
			if (indexOf == -1) {
				_endIndex = _startIndex;
			} else {
				_endIndex = indexOf;
			}
		} else {
			int indexOf = new StringBuilder(string).reverse().indexOf(Character.toString(_character), length - startIndex);
			if (indexOf == -1) {
				_endIndex = startIndex;
			} else {
				_endIndex = length - 1 - indexOf;
			}
		}
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		int i = 0;
		for (Event event : nextEvent) {
			if (!event.isCharacter()) return RecognitionState.NotRecognized;
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
				if (!event.isCharacter()) return RecognitionState.NotRecognized;
			}
			i++;
		}
		if (i < 2) return RecognitionState.MaybeRecognized;
		return RecognitionState.Recognized;
	}

	@Override
	public MotionRange getRange() {
		findIndex();
		return new MotionRange(_startIndex, _endIndex);
	}

}
