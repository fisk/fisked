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

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.IApplication;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.ui.drawing.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class WindowManager implements IWindowManager {
	private final static Logger LOG = LoggerFactory.getLogger(WindowManager.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(WindowManager.class);

	private ConcurrentHashMap<String, Window> _map;
	private final Stack<Screen> _primaryScreenStack = new Stack<>();

	public WindowManager() {
		Screen screen = new Screen("Primordial Screen");
		pushPrimaryScreen(screen);
	}

	@Override
	public Window getWindow(String name) {
		return _map.get(name);
	}

	@Override
	public Window registerWindow(Window window) {
		Window prev = _map.putIfAbsent(window.getId(), window);
		if (prev == null) {
			return window;
		} else {
			return prev;
		}
	}

	@Validate
	public void start() {
		_map = new ConcurrentHashMap<>();
	}

	@Invalidate
	public void stop() {
		_map = null;
	}

	@Override
	public Screen getPrimaryScreen() {
		return _primaryScreenStack.peek();
	}

	private void exit(int status) {
		try (IBehaviorConnection<IApplication> applicationBC = BEHAVIORS.getBehaviorConnection(IApplication.class)
				.get()) {
			applicationBC.getBehavior().exit(0);
		} catch (Exception e) {
			LOG.error("Could not exit application: ", e);
		}
	}

	@Override
	public void pushPrimaryScreen(Screen screen) {
		if (screen == null) {
			throw new RuntimeException("Trying to push null screen");
		}
		LOG.debug("Pushed screen: " + screen);
		_primaryScreenStack.push(screen);
		screen.fullRedraw();
	}

	@Override
	public Screen popPrimaryScreen() {
		if (_primaryScreenStack.size() <= 1) {
			exit(0);
		}
		_primaryScreenStack.pop();
		Screen screen = _primaryScreenStack.peek();
		if (screen == null) {
			exit(0);
		} else {
			screen.fullRedraw();
		}
		LOG.debug("Popped window: " + screen);
		return screen;
	}

	@Override
	public void unregisterWindow(Window window) {
		_map.remove(window.getName(), window);
	}

}
