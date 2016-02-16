package org.fisked;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.drawing.Window;
import org.fisked.command.CommandManager;
import org.fisked.command.OpenFileCommand;
import org.fisked.language.eval.service.ISourceEvaluator;
import org.fisked.launcher.service.ILauncherService;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.EventLoop;
import org.fisked.services.ComponentManager;
import org.fisked.services.ServiceManager;
import org.fisked.shell.ShellCommandHandler;
import org.fisked.util.ConsolePrinter;
import org.fisked.util.FileUtil;
import org.fisked.util.concurrency.Dispatcher;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jline.internal.Log;

public class Application {
	private final static Logger LOG = LoggerFactory.getLogger(Application.class);
	private static volatile Application _application;

	public static Application getApplication() {
		return _application;
	}

	private EventLoop _loop;
	private Window _primaryWindow;
	private volatile Throwable _exception;
	private String[] _argv;

	public EventLoop getEventLoop() {
		return _loop;
	}

	public Window getPrimaryWindow() {
		return _primaryWindow;
	}

	private void evaluateScript(BufferWindow window, String language) {
		if (window.getBufferController().getSelection() != null) {
			LOG.debug("Evaluating script of type: " + language);
			String text = window.getBufferController().getSelectedText();
			ISourceEvaluator evaluator = ComponentManager.getInstance().getSourceEvalManager().getEvaluator(language);
			if (evaluator != null) {
				LOG.debug("Running script:\n" + text);
				String result;
				try {
					result = evaluator.evaluate(text);
					Log.debug("Result: " + result);
				} catch (Throwable e) {
					result = e.getMessage();
				}
				if (result != null) {
					window.getBufferController().setSelectionText(result);
					window.switchToNormalMode();
				} else {
					LOG.debug("Evaluator did not reply.");
					window.getCommandController().setCommandFeedback("Evaluator did not reply.");
				}
			} else {
				LOG.debug("Could not find evaluator for " + language);
				ComponentManager.getInstance().getLauncherService().printBundles();
				window.getCommandController().setCommandFeedback("Could not find evaluator for " + language);
			}
		} else {
			window.getCommandController().setCommandFeedback("Can't evaluate script without selection.");
		}
	}

	private void setupCommands() {
		CommandManager cm = CommandManager.getSingleton();
		cm.registerHandler("q", (BufferWindow window, String[] argv) -> {
			exit(0);
		});
		cm.registerHandler("e", new OpenFileCommand());
		cm.registerHandler("w", (BufferWindow window, String[] argv) -> {
			try {
				window.getBuffer().save();
				window.getCommandController().setCommandFeedback("Saved file.");
			} catch (Exception e) {
				window.getCommandController().setCommandFeedback("Couldn't save.");
			}
		});
		cm.registerHandler("r", new ShellCommandHandler());
		cm.registerHandler("ruby", (BufferWindow window, String[] argv) -> {
			evaluateScript(window, "ruby");
		});
		cm.registerHandler("python", (BufferWindow window, String[] argv) -> {
			evaluateScript(window, "python");
		});
		cm.registerHandler("javascript", (BufferWindow window, String[] argv) -> {
			evaluateScript(window, "javascript");
		});
		cm.registerHandler("lisp", (BufferWindow window, String[] argv) -> {
			evaluateScript(window, "lisp");
		});
		cm.registerHandler("groovy", (BufferWindow window, String[] argv) -> {
			evaluateScript(window, "groovy");
		});
		cm.registerHandler("script", (BufferWindow window, String[] argv) -> {
			if (argv.length != 2)
				return;
			File file = FileUtil.getFile(argv[1]);
			ISourceEvaluator evaluator = ComponentManager.getInstance().getSourceEvalManager().getEvaluator(file);
			if (evaluator == null)
				return;
			String string;
			try {
				string = FileUtils.readFileToString(file);
				string = evaluator.evaluate(string);
			} catch (Exception e) {
				string = e.getMessage();
			}
			window.getBufferController().getBuffer().appendStringAtPointLogged(string);
			window.switchToNormalMode();
		});
	}

	private void processArguments() {
		BufferWindow window = (BufferWindow) _primaryWindow;
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

	private Thread _shutdownHook;

	private void shutDownServices() {
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();
		cs.getCursorService().changeCursor(ICursorService.CURSOR_BLOCK);
		cs.deactivate();
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

			setupCommands();
			ServiceManager sm = ServiceManager.getInstance();
			IConsoleService cs = sm.getConsoleService();

			cs.activate();

			Rectangle windowRect = new Rectangle(0, 0, cs.getScreenWidth(), cs.getScreenHeight());

			_primaryWindow = new BufferWindow(windowRect);
			processArguments();

			_loop.setPrimaryResponder(_primaryWindow);

			_primaryWindow.draw();
			// ComponentManager cm = ComponentManager.getInstance();
			// cm.sendEmail();

			_loop.start();
		} catch (Throwable throwable) {
			Application app = Application.getApplication();
			app.setException(throwable);
			app.exit(-1);
		}
	}

	public void setException(Throwable throwable) {
		_exception = throwable;
	}

	public void exit(int code) {
		LOG.debug("Exiting fisked gracefully.");
		_loop.exit();
		shutDownServices();
		Runtime.getRuntime().removeShutdownHook(_shutdownHook);
		ILauncherService launcher = ComponentManager.getInstance().getLauncherService();
		launcher.stop(code);
	}

	public Application(ILauncherService launcherService, BundleContext context) {
		_application = this;
	}
}
