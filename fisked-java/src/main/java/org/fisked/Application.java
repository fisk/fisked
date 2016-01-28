package org.fisked;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.drawing.Window;
import org.fisked.command.CommandManager;
import org.fisked.command.OpenFileCommand;
import org.fisked.language.ISourceEvaluator;
import org.fisked.language.SourceEvaluatorManager;
import org.fisked.launcher.service.ILauncherService;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.EventLoop;
import org.fisked.services.ServiceManager;
import org.fisked.shell.ShellCommandHandler;
import org.fisked.util.ConsolePrinter;
import org.fisked.util.FileUtil;
import org.fisked.util.concurrency.Dispatcher;

import jline.internal.Log;

public class Application {
	final static Logger LOG = LogManager.getLogger(Application.class);
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
			ISourceEvaluator evaluator = SourceEvaluatorManager.getInstance().getEvaluator(language);
			LOG.debug("Running script:\n" + text);
			String result = evaluator.evaluate(text);
			Log.debug("Result: " + result);
			window.getBufferController().setSelectionText(result);
			window.switchToNormalMode();
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
		cm.registerHandler("script", (BufferWindow window, String[] argv) -> {
			if (argv.length != 2)
				return;
			File file = FileUtil.getFile(argv[1]);
			ISourceEvaluator evaluator = SourceEvaluatorManager.getInstance().getEvaluator(file);
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
		LOG.debug("Starting Fisked");
		_argv = argv;
		try {
			_shutdownHook = new Thread() {
				@Override
				public void run() {
					shutDownServices();
				}
			};
			Runtime.getRuntime().addShutdownHook(_shutdownHook);

			Dispatcher.getInstance().setMainThread(Thread.currentThread());

			setupCommands();
			_loop = new EventLoop();
			ServiceManager sm = ServiceManager.getInstance();
			IConsoleService cs = sm.getConsoleService();

			cs.activate();

			Rectangle windowRect = new Rectangle(0, 0, cs.getScreenWidth(), cs.getScreenHeight());

			_primaryWindow = new BufferWindow(windowRect);
			processArguments();

			_loop.setPrimaryResponder(_primaryWindow);

			_primaryWindow.draw();

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
		LOG.debug("Recognized quit command.");
		_loop.exit();
		shutDownServices();
		Runtime.getRuntime().removeShutdownHook(_shutdownHook);
		_launcherService.stop(code);
	}

	private final ILauncherService _launcherService;

	public Application(ILauncherService launcherService) {
		_launcherService = launcherService;
		_application = this;
	}
}
