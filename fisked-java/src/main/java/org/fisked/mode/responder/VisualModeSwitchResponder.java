package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.models.selection.SelectionMode;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;

public class VisualModeSwitchResponder implements IInputResponder {
	private final BufferWindow _window;
	private SelectionMode _mode;

	public VisualModeSwitchResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (input.isCharacter('v')) {
			_mode = SelectionMode.NORMAL_MODE;
			return RecognitionState.Recognized;
		}
		if (input.isCharacter('V')) {
			_mode = SelectionMode.LINE_MODE;
			return RecognitionState.Recognized;
		}
		if (input.isControlChar('v')) {
			_mode = SelectionMode.BLOCK_MODE;
			return RecognitionState.Recognized;
		}
		return RecognitionState.NotRecognized;
	}

	@Override
	public void onRecognize() {
		_window.switchToVisualMode(_mode);
	}

}
