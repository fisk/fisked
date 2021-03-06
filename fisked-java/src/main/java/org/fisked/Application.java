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
package org.fisked;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.command.api.ICommandManager;
import org.fisked.launcher.service.ILauncherService;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.responder.EventLoop;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.ui.screen.Screen;
import org.fisked.ui.window.IWindowManager;
import org.fisked.util.ConsolePrinter;
import org.fisked.util.FileUtil;
import org.fisked.util.concurrency.Dispatcher;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = { IApplication.class })
public class Application implements IApplication {
	private final static Logger LOG = LoggerFactory.getLogger(Application.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(Application.class);

	private EventLoop _loop;
	private volatile Throwable _exception;
	private String[] _argv;
	private Thread _shutdownHook;
	private Thread _mainThread;
	private ILauncherService _launcher;

	@Requires
	private IConsoleService _consoleService;

	@Requires
	private ICommandManager _commandManager;

	@Requires
	private IWindowManager _windowManager;

	private String state(int state) {
		switch (state) {
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.ACTIVE:
			return "ACTIVE";
		default:
			return "UNKNOWN";
		}
	}

	@Validate
	public void start() {
		LOG.trace("Starting application.");
		try {
			Future<IBehaviorConnection<ILauncherService>> launcherFuture = BEHAVIORS
					.getBehaviorConnection(ILauncherService.class);
			if (!launcherFuture.isDone()) {
				LOG.error("Could not start application as there is no initial launcher registered.");
				return;
			}

			try (IBehaviorConnection<ILauncherService> launcherBC = launcherFuture.get()) {
				ILauncherService launcher = launcherBC.getBehavior();
				start(launcher);
			} catch (Exception e) {
				LOG.error("Could not launch application: ", e);
			}
		} catch (Exception e) {
			LOG.error("Could not start application as there is no initial launcher registered.");
			return;
		}
	}

	@Invalidate
	public void stop() {
	}

	public EventLoop getEventLoop() {
		return _loop;
	}

	private void processArguments(BufferWindow window) {
		if (_argv.length == 1) {
			String path = _argv[0];
			File file = FileUtil.getFile(path);
			try {
				window.openFile(file);
			} catch (IOException e) {
				e.printStackTrace();
				exit(-1);
			}
		}
	}

	private void shutDownServices() {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			IConsoleService cs = consoleBC.getBehavior();
			cs.getCursorService().changeCursor(ICursorService.CURSOR_BLOCK);
			cs.deactivate();
		} catch (Exception e) {
			LOG.error("Can't get console service: ", e);
		}

		if (_exception != null) {
			ConsolePrinter.LOG.error("Fisked exited because of an exception:", _exception);
		}
	}

	public void start(String[] argv) {
		LOG.debug("Starting Fisked.");
		_argv = argv;
		try {
			LOG.debug("Starting run loop.");
			_loop = new EventLoop();
			Dispatcher.getInstance().setMainThread(_loop, Thread.currentThread());

			_shutdownHook = new Thread() {
				@Override
				public void run() {
					shutDownServices();
				}
			};
			Runtime.getRuntime().addShutdownHook(_shutdownHook);

			try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
					.get()) {
				IConsoleService cs = consoleBC.getBehavior();
				cs.activate();

				Screen screen = _windowManager.getPrimaryScreen();
				BufferWindow window = new BufferWindow(screen.getBounds(), "Main Window");
				processArguments(window);

				screen.attachWindow(window);
				screen.fullRedraw();
			} catch (Exception e) {
				LOG.error("Can't get console service: ", e);
			}

			_loop.start();
		} catch (Throwable throwable) {
			Application app = Application.this;
			app.setException(throwable);
			app.exit(-1);
		}
	}

	public void setException(Throwable throwable) {
		_exception = throwable;
	}

	@Override
	public void exit(int code) {
		LOG.debug("Exiting fisked gracefully.");
		if (Thread.currentThread() != _mainThread) {
			Dispatcher.getInstance().runMain(() -> {
				exit(code);
			});
			return;
		}
		_loop.exit();
		shutDownServices();
		Runtime.getRuntime().removeShutdownHook(_shutdownHook);
		if (_launcher != null) {
			_launcher.stop(code);
		}
	}

	@Override
	public void start(ILauncherService launcher) {
		LOG.trace("Starting application with launcher.");
		_launcher = launcher;
		final String[] args = launcher.getMainArgs();
		if (args != null) {
			_mainThread = new Thread() {
				@Override
				public void run() {
					setName("Fisked Main Thread");
					Application.this.start(args);
				}
			};
			_mainThread.start();
		}
	}
}
