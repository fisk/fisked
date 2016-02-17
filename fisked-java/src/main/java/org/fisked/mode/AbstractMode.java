package org.fisked.mode;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.IRecognitionAction;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.RecognitionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMode implements IInputResponder {
	private final static Logger LOG = LoggerFactory.getLogger(AbstractMode.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(AbstractMode.class);
	protected BufferWindow _window;
	protected InputResponderChain _responders = new InputResponderChain();

	public AbstractMode(BufferWindow window) {
		_window = window;
	}

	abstract public String getModeName();

	protected void addResponder(IInputRecognizer responder) {
		_responders.addResponder(responder);
	}

	protected void addResponder(IInputResponder responder) {
		_responders.addResponder(responder);
	}

	protected void addResponder(IInputRecognizer responder, IRecognitionAction callback) {
		_responders.addResponder(responder, callback);
	}

	protected void addResponder(String match, IRecognitionAction callback) {
		_responders.addResponder(match, callback);
	}

	public abstract Face getModelineFace();

	public void activate() {
	}

	public void deactivate() {
	}

	protected final int CURSOR_BLOCK = 0;
	protected final int CURSOR_VERTICAL_BAR = 1;
	protected final int CURSOR_UNDERLINE = 2;

	protected void changeCursor(int cursor) {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			consoleBC.getBehavior().getCursorService().changeCursor(cursor);
		} catch (Exception e) {
			LOG.error("Exception in changing cursor: ", e);
		}
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		return _responders.recognizesInput(input);
	}

	@Override
	public void onRecognize() {
		_responders.onRecognize();
	}

	public BufferWindow getWindow() {
		return _window;
	}
}
