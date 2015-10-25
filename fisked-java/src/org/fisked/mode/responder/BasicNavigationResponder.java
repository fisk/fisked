package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.RecognitionState;
import org.fisked.responder.motion.IMotion.MotionRange;
import org.fisked.responder.motion.MotionRecognizer;
import org.fisked.text.TextNavigator;

public class BasicNavigationResponder implements IInputResponder {
	private BufferWindow _window;
	private TextNavigator _navigator;
	private InputResponderChain _responders = new InputResponderChain();

	public BasicNavigationResponder(BufferWindow window) {
		_window = window;
		_navigator = new TextNavigator(_window.getBuffer());
		final MotionRecognizer motionRecognizer = new MotionRecognizer(window);
		_responders.addResponder(motionRecognizer, () -> {
			MotionRange range = motionRecognizer.getRange();
			_navigator.moveToIndexAndScroll(range.getEnd());
		});
		_responders.addResponder((Event nextEvent) -> {
			if (nextEvent.isControlChar('e')) return RecognitionState.Recognized;
			return RecognitionState.NotRecognized;
		}, () -> {
			_navigator.scrollDown();
			_navigator.moveCursorDownIfNeeded();
		});
		_responders.addResponder((Event nextEvent) -> {
			if (nextEvent.isControlChar('y')) return RecognitionState.Recognized;
			return RecognitionState.NotRecognized;
		}, () -> {
			_navigator.scrollUp();
			_navigator.moveCursorUpIfNeeded();
		});
		_responders.addResponder("h", () -> {
			_navigator.moveLeft();
			_navigator.scrollUpIfNeeded();
		});
		_responders.addResponder("l", () -> {
			_navigator.moveRight();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("j", () -> {
			_navigator.moveDown();
			_navigator.scrollDownIfNeeded();
		});
		_responders.addResponder("k", () -> {
			_navigator.moveUp();
			_navigator.scrollUpIfNeeded();
		});
	}

	@Override
	public RecognitionState handleInput(Event input) {
		return _responders.handleInput(input);
	}

}
