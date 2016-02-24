package org.fisked;

import java.io.File;
import java.io.IOException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.drawing.Window;
import org.fisked.launcher.service.ILauncherService;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.EventLoop;
import org.fisked.util.ConsolePrinter;
import org.fisked.util.FileUtil;
import org.fisked.util.concurrency.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = { IApplication.class })
public class Application implements IApplication {
	private final static Logger LOG = LoggerFactory.getLogger(Application.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(Application.class);

	private EventLoop _loop;
	private Window _primaryWindow;
	private volatile Throwable _exception;
	private String[] _argv;
	private Thread _shutdownHook;

	@Validate
	public void start() {
		try (IBehaviorConnection<ILauncherService> launcherBC = BEHAVIORS.getBehaviorConnection(ILauncherService.class)
				.get()) {
			final String[] args = launcherBC.getBehavior().getMainArgs();
			Thread thread = new Thread() {
				@Override
				public void run() {
					setName("Fisked Main Thread");
					Application.this.start(args);
				}
			};
			thread.start();
		} catch (Exception e) {
			LOG.error("Could not launch application: ", e);
		}

	}

	@Invalidate
	public void stop() {
	}

	public EventLoop getEventLoop() {
		return _loop;
	}

	@Override
	public Window getPrimaryWindow() {
		return _primaryWindow;
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

				Rectangle windowRect = new Rectangle(0, 0, cs.getScreenWidth(), cs.getScreenHeight());
				BufferWindow window = new BufferWindow(windowRect);
				processArguments(window);

				setPrimaryWindow(window);
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

	@Override
	public void setPrimaryWindow(Window window) {
		_primaryWindow = window;
		_loop.setPrimaryResponder(_primaryWindow);
		_primaryWindow.draw();
	}

	public void setException(Throwable throwable) {
		_exception = throwable;
	}

	@Override
	public void exit(int code) {
		LOG.debug("Exiting fisked gracefully.");
		_loop.exit();
		shutDownServices();
		Runtime.getRuntime().removeShutdownHook(_shutdownHook);

		try (IBehaviorConnection<ILauncherService> launcherBC = BEHAVIORS.getBehaviorConnection(ILauncherService.class)
				.get()) {
			launcherBC.getBehavior().stop(code);
		} catch (Exception e) {
			LOG.error("Can't find launcher service: ", e);
		}
	}
}
