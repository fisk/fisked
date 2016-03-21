package org.fisked.email.ui;

import java.util.Stack;

import org.fisked.IApplication;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.drawing.View;
import org.fisked.buffer.drawing.Window;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.InputResponderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailWindow extends Window {
	private final static Logger LOG = LoggerFactory.getLogger(EmailWindow.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(EmailWindow.class);

	private final EmailController _controller = new EmailController(this);
	private final Stack<InputResponderChain> _responderChainStack = new Stack<>();

	public EmailController getController() {
		return _controller;
	}

	public EmailWindow(Rectangle windowRect) {
		super(windowRect);

		Rectangle rootViewRect = windowRect;

		_rootView = new View(rootViewRect);

		InputResponderChain chain = new InputResponderChain();

		chain.addResponder("q", () -> {
			try (IBehaviorConnection<IApplication> applicationBC = BEHAVIORS.getBehaviorConnection(IApplication.class)
					.get()) {
				LOG.debug("Quit command being invoked.");
				applicationBC.getBehavior().popPrimaryWindow();
			} catch (Exception e) {
				LOG.error("Could not pop window: ", e);
			}
		});

		_primaryResponder = chain;
		_responderChainStack.push(chain);
	}

	public void pushResponderChain(InputResponderChain chain) {
		InputResponderChain parent = _responderChainStack.peek();
		chain.setParent(parent);
		_primaryResponder = chain;
	}

	public void popResponderChain() {
		_responderChainStack.pop();
		InputResponderChain top = _responderChainStack.peek();
		_primaryResponder = top;
	}

}
