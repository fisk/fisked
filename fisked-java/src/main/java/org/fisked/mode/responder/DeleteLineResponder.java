package org.fisked.mode.responder;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;

public class DeleteLineResponder implements IInputResponder {

	private final BufferWindow _window;

	public DeleteLineResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matchesExact(nextEvent, "dd");
	}

	private int getLineStart() {
		Buffer buffer = _window.getBuffer();
		String string = buffer.getCharSequence().toString();

		int newIndex = buffer.getPointIndex();
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

	private int getLineEnd() {
		Buffer buffer = _window.getBuffer();
		String string = buffer.getCharSequence().toString();

		int newIndex = buffer.getPointIndex();
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
		int start = getLineStart();
		int lineEnd = getLineEnd() + 1;
		int end = Math.min(lineEnd, buffer.length());
		start -= lineEnd - end;
		start = Math.max(start, 0);
		Range lineRange = new Range(start, end - start);

		if (lineRange.getLength() != 0) {
			buffer.removeCharsInRangeLogged(lineRange);
		}
		_window.setNeedsFullRedraw();
	}

}
