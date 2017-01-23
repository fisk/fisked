/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
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
package org.fisked.ui.window;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;
import org.fisked.ui.drawing.IDrawable;
import org.fisked.ui.drawing.Screen;
import org.fisked.ui.drawing.View;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Window implements IInputResponder, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(Window.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(Window.class);

	protected View _rootView;
	protected Rectangle _windowRect;
	protected IInputResponder _primaryResponder;
	protected String _name;
	protected String _id;
	protected Screen _screen;

	public Window(Rectangle windowRect, String name) {
		_windowRect = windowRect;
		_name = name;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public void attachScreen(Screen screen) {
		_screen = screen;
	}

	public void detatchScreen() {
		_screen = null;
	}

	public Screen getAttachedScreen() {
		return _screen;
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

	public String getName() {
		return _name;
	}

	public void register() {
		if (_id == null) {
			return;
		}
		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			wmBC.getBehavior().registerWindow(this);
		} catch (Exception e) {
			LOG.error("Can't get window manager service: ", e);
		}
	}

	public void unregister() {
		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			wmBC.getBehavior().unregisterWindow(this);
		} catch (Exception e) {
			LOG.error("Can't get window manager service: ", e);
		}
	}

}
