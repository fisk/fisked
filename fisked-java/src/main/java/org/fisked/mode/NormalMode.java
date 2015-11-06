package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.InputModeSwitchResponder;
import org.fisked.mode.responder.MotionActionResponder;
import org.fisked.mode.responder.VisualModeSwitchResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;
import org.fisked.services.ServiceManager;
import org.fisked.text.TextNavigator;

public class NormalMode extends AbstractMode {

	public String getClipboard() {
		return ServiceManager.getInstance().getClipboardService().getClipboard();
	}

	public NormalMode(BufferWindow window) {
		super(window);
		addResponder(new CommandInputResponder(_window));
		addResponder(new InputModeSwitchResponder(_window));
		addResponder(new VisualModeSwitchResponder(_window));
		addResponder(new BasicNavigationResponder(_window));
		addResponder(new MotionActionResponder(_window));
		addResponder((Event nextEvent) -> {
			if (nextEvent.isCharacter('p')) {
				_window.getBuffer().appendStringAtPoint(getClipboard());
				_window.switchToNormalMode();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder((Event nextEvent) -> {
			if (nextEvent.isCharacter('P')) {
				TextNavigator navigator = new TextNavigator(_window);
				navigator.moveLeft();
				_window.getBuffer().appendStringAtPoint(getClipboard());
				_window.switchToNormalMode();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
	}

	@Override
	public Face getModelineFace() {
		return new Face(Color.MAGENTA, Color.WHITE);
	}

	@Override
	public void activate() {
		changeCursor(CURSOR_UNDERLINE);
	}

	@Override
	public String getModeName() {
		return "normal";
	}

}
