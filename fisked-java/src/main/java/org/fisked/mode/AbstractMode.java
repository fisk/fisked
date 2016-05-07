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
