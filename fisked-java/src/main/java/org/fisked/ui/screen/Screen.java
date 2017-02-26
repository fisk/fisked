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
package org.fisked.ui.screen;

import java.util.HashSet;
import java.util.Set;

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
	private final WindowContainer _rootContainer = new WindowContainer();
	private Window _primaryWindow;
	private final String _name;
	private Size _size;

	private WindowContainer getContainer(Window window) {
		WindowContainer container = _rootContainer.find(window);
		return container;
	}

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
			Rectangle clippingRect = new Rectangle(0, 0, consoleBC.getBehavior().getScreenWidth(),
					consoleBC.getBehavior().getScreenHeight());
			try (IRenderingContext context = consoleBC.getBehavior().getRenderingContext(clippingRect)) {
				ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
				context.clearScreen(theme.getBackgroundColor());

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

	public void attachWindowVertical(Window window) {
		LOG.debug("#1");
		Window primaryWindow = getPrimaryWindow();
		WindowContainer primaryContainer = getContainer(primaryWindow);
		WindowContainer container = getContainer(window);
		LOG.debug("#1");
		if (container == null) {
			LOG.debug("#2");
			container = new WindowContainer();
			container.setWindow(window);
		}
		LOG.debug("#3" + container + primaryContainer + primaryWindow + window);
		WindowContainer primaryParentContainer = primaryContainer.getParent();
		primaryContainer.removeFromParent();
		LOG.debug("#4");

		WindowContainer newParentContainer = new WindowContainer();
		newParentContainer.addChild(primaryContainer);
		newParentContainer.addChild(container);

		primaryParentContainer.addChild(newParentContainer);

		Rectangle rect = primaryWindow.getWindowRect();
		int height = rect.getSize().getHeight();
		int topHeight = height / 2;
		int bottomHeight = height - topHeight;
		Rectangle bottomRect = new Rectangle(rect.getOrigin().getX(), rect.getOrigin().getY() + topHeight,
				rect.getSize().getWidth(), bottomHeight);
		Rectangle topRect = new Rectangle(rect.getOrigin().getX(), rect.getOrigin().getY(), rect.getSize().getWidth(),
				topHeight);

		LOG.debug("Top rect: " + topRect);
		LOG.debug("Bottom rect: " + bottomRect);

		window.setWindowRect(bottomRect);
		primaryWindow.setWindowRect(topRect);

		addWindow(window);
		window.attachScreen(this);
		if (isPrimary()) {
			fullRedraw();
		}
	}

	public void attachWindow(Window window) {
		WindowContainer container = new WindowContainer();
		container.setWindow(window);
		_rootContainer.addChild(container);
		addWindow(window);
		window.attachScreen(this);
		if (isPrimary()) {
			fullRedraw();
		}
	}

	public void detatchWindow(Window window) {
		WindowContainer container = getContainer(window);
		if (container != null) {
			container.clearWindow();
			WindowContainer parentContainer = container.getParent();
			Window otherWindow = null;
			for (WindowContainer sibling : parentContainer.getChildren()) {
				if (sibling != container) {
					otherWindow = sibling.getWindow();
				}
			}

			int minY = Math.min(window.getWindowRect().getOrigin().getY(),
					otherWindow.getWindowRect().getOrigin().getY());
			int maxY = Math.min(
					window.getWindowRect().getOrigin().getY() + window.getWindowRect().getSize().getHeight(),
					otherWindow.getWindowRect().getOrigin().getY() + otherWindow.getWindowRect().getSize().getHeight());
			int height = maxY - minY;

			Rectangle rect = new Rectangle(otherWindow.getWindowRect().getOrigin().getX(), minY,
					otherWindow.getWindowRect().getSize().getWidth(), height);
			otherWindow.setWindowRect(rect);
		}
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
