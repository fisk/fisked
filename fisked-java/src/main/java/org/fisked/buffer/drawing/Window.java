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
package org.fisked.buffer.drawing;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Window implements IInputResponder, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(Window.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(Window.class);

	protected View _rootView;
	protected Rectangle _windowRect;
	protected IInputResponder _primaryResponder;

	public Window(Rectangle windowRect) {
		_windowRect = windowRect;
	}

	public void setRootView(View rootView) {
		_rootView = rootView;
	}

	public View getRootView() {
		return _rootView;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		IInputResponder responder = _primaryResponder;
		if (responder == null)
			return RecognitionState.NotRecognized;
		RecognitionState status = responder.recognizesInput(input);
		if (status == RecognitionState.Recognized) {
			setNeedsFullRedraw();
		}
		return status;
	}

	@Override
	public void onRecognize() {
		IInputResponder responder = _primaryResponder;
		if (responder != null)
			responder.onRecognize();
	}

	@Override
	public void draw() {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			try (IRenderingContext context = consoleBC.getBehavior().getRenderingContext()) {
				ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();

				if (_needsFullRedraw) {
					context.clearScreen(theme.getBackgroundColor());
					_rootView.draw();
				}
				_needsLineRedraw = false;
				_needsFullRedraw = false;

				drawPoint(context);
			}
		} catch (Exception e) {
			LOG.error("Can't get console service: ", e);
		}
	}

	public void drawPoint(IRenderingContext context) {

	}

	protected boolean _needsFullRedraw = true;
	protected boolean _needsLineRedraw = false;

	public void setNeedsFullRedraw() {
		_needsFullRedraw = true;
	}

	public void setNeedsLineRedraw() {
		_needsLineRedraw = true;
	}

	public void refresh() {
		draw();
	}

}
