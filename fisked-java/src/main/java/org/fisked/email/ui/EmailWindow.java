/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
