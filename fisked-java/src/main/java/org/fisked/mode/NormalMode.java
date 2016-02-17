package org.fisked.mode;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.DeleteLineResponder;
import org.fisked.mode.responder.InputModeSwitchResponder;
import org.fisked.mode.responder.MotionActionResponder;
import org.fisked.mode.responder.VisualModeSwitchResponder;
import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;
import org.fisked.text.TextNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalMode extends AbstractMode {
	private final static Logger LOG = LoggerFactory.getLogger(NormalMode.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(NormalMode.class);

	public NormalMode(BufferWindow window) {
		super(window);
		addResponder(new CommandInputResponder(_window));
		addResponder(new InputModeSwitchResponder(_window));
		addResponder(new VisualModeSwitchResponder(_window));
		addResponder(new BasicNavigationResponder(_window));
		addResponder(new MotionActionResponder(_window));
		addResponder(nextEvent -> {
			if (nextEvent.isCharacter('p')) {
				try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
						.getBehaviorConnection(IClipboardService.class).get()) {
					_window.getBuffer().appendStringAtPointLogged(clipboardBC.getBehavior().getClipboard());
				} catch (Exception e) {
					LOG.error("Exception in clipboard: ", e);
				}
				_window.switchToNormalMode();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder(nextEvent -> {
			if (nextEvent.isCharacter('P')) {
				TextNavigator navigator = new TextNavigator(_window);
				navigator.moveLeft();
				try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
						.getBehaviorConnection(IClipboardService.class).get()) {
					_window.getBuffer().appendStringAtPointLogged(clipboardBC.getBehavior().getClipboard());
				} catch (Exception e) {
					LOG.error("Exception in clipboard: ", e);
				}
				_window.switchToNormalMode();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder(nextEvent -> {
			return EventRecognition.matchesExact(nextEvent, "u");
		} , () -> {
			_window.getBuffer().undo();
			_window.setNeedsFullRedraw();
		});
		addResponder(nextEvent -> {
			return nextEvent.isControlChar('r') ? RecognitionState.Recognized : RecognitionState.NotRecognized;
		} , () -> {
			_window.getBuffer().redo();
			_window.setNeedsFullRedraw();
		});
		addResponder(new DeleteLineResponder(_window));
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
