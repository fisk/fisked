package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;
import org.fisked.text.TextNavigator;

public class InputModeSwitchResponder implements IInputRecognizer {
	private final BufferWindow _window;
	private final TextNavigator _navigator;

	public InputModeSwitchResponder(BufferWindow window) {
		_window = window;
		_navigator = new TextNavigator(_window);
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		if (input.isCharacter('i')) {
			_window.switchToInputMode(0);
			return RecognitionState.Recognized;
		}
		if (input.isCharacter('a')) {
			_navigator.moveRight();
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode(0);
			return RecognitionState.Recognized;
		}
		if (input.isCharacter('A')) {
			_navigator.moveToTheEndOfLine();
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode(0);
			return RecognitionState.Recognized;
		}
		if (input.isCharacter('o')) {
			_navigator.moveToTheEndOfLine();
			_window.getBuffer().appendCharAtPointLogged('\n');
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode(1);
			return RecognitionState.Recognized;
		}
		if (input.isCharacter('O')) {
			_navigator.moveToTheBeginningOfLine();
			_window.getBuffer().appendCharAtPointLogged('\n');
			_navigator.moveLeft();
			_navigator.scrollDownIfNeeded();
			_window.switchToInputMode(1);
			return RecognitionState.Recognized;
		}
		return RecognitionState.NotRecognized;
	}

}
