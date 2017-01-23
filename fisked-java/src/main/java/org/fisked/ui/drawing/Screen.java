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
package org.fisked.ui.drawing;

import java.util.HashSet;
import java.util.Set;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.ui.window.IWindowManager;
import org.fisked.ui.window.Window;
import org.fisked.util.models.Rectangle;
import org.fisked.util.models.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Screen implements IInputResponder, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(Screen.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(Screen.class);

	private final Set<Window> _windows = new HashSet<>();
	private Window _primaryWindow;
	private final String _name;
	private Size _size;

	public Screen(String name) {
		_name = name;
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			IConsoleService cs = consoleBC.getBehavior();
			_size = new Size(cs.getScreenWidth(), cs.getScreenHeight());
		} catch (Exception e) {
			LOG.error("Could not get console service", e);
		}
	}

	public Size getSize() {
		return _size;
	}

	public Rectangle getBounds() {
		return new Rectangle(0, 0, _size.getWidth(), _size.getHeight());
	}

	public String getName() {
		return _name;
	}

	private void removeWindow(Window window) {
		_windows.remove(window);
		if (_primaryWindow == window) {
			_primaryWindow = null;
		}
	}

	protected void addWindow(Window window) {
		_windows.add(window);
		if (_primaryWindow == null) {
			_primaryWindow = window;
		}
	}

	public void setPrimaryWindow(Window window) {
		_primaryWindow = window;
	}

	public Window getPrimaryWindow() {
		return _primaryWindow;
	}

	@Override
	public void draw() {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			try (IRenderingContext context = consoleBC.getBehavior().getRenderingContext()) {
				for (Window window : _windows) {
					window.draw();
				}
				Window primary = _primaryWindow;
				if (primary != null) {
					primary.drawPoint(context);
				}
			}
		} catch (Exception e) {
			LOG.error("Can't get console service: ", e);
		}
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return _primaryWindow.recognizesInput(nextEvent);
	}

	@Override
	public void onRecognize() {
		_primaryWindow.onRecognize();
	}

	public void addVerticalSplit(Window window) {
		attachWindow(window);
	}

	public void addHorizontalSplit(Window window) {
		attachWindow(window);
	}

	public void adjustExpandVertical(int space) {
		throw new RuntimeException("Not implemented yet");
	}

	public void adjustDetractVertical(int space) {
		throw new RuntimeException("Not implemented yet");
	}

	public void adjustExpandHorizontal(int space) {
		throw new RuntimeException("Not implemented yet");
	}

	public void adjustDetractHorizontal(int space) {
		throw new RuntimeException("Not implemented yet");
	}

	public boolean isPrimary() {
		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			IWindowManager wm = wmBC.getBehavior();
			return wm.getPrimaryScreen() == this;
		} catch (Exception e) {
			LOG.error("Could not get window manager service", e);
			return false;
		}
	}

	public void attachWindow(Window window) {
		addWindow(window);
		window.attachScreen(this);
		if (isPrimary()) {
			fullRedraw();
		}
	}

	public void detatchWindow(Window window) {
		removeWindow(window);
		window.detatchScreen();
		if (isPrimary()) {
			fullRedraw();
		}
	}

	public void fullRedraw() {
		setNeedsFullRedraw();
		draw();
	}

	public void setNeedsFullRedraw() {
		for (Window window : _windows) {
			window.setNeedsFullRedraw();
		}
	}
}
