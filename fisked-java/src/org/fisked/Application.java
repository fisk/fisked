package org.fisked;

import java.io.File;
import java.io.IOException;

import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.drawing.Window;
import org.fisked.command.CommandManager;
import org.fisked.command.OpenFileCommand;
import org.fisked.log.Log;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.EventLoop;
import org.fisked.services.ServiceManager;
import org.fisked.shell.ShellCommandHandler;
import org.fisked.util.FileUtil;

public class Application {
	private static Application _sharedInstance;
	public static Application getApplication() {
		if (_sharedInstance != null) return _sharedInstance;
		synchronized (Application.class) {
			if (_sharedInstance == null) {
				_sharedInstance = new Application();
			}
		}
		return _sharedInstance;
	}
	
	private EventLoop _loop;
	private Window _primaryWindow;
	private volatile Throwable _exception;
	private String[] _argv;
	
	private void setupCommands() {
		CommandManager cm = CommandManager.getSingleton();
		cm.registerHandler("q", (BufferWindow window, String[] argv) -> { 
			System.exit(0);
			});
		cm.registerHandler("e", new OpenFileCommand());
		cm.registerHandler("w", (BufferWindow window, String[] argv) -> { 
			try {
				window.getBuffer().save();
			} catch (Exception e) {
				window.getCommandController().setCommandFeedback("Couldn't save.");
			}
			});
		cm.registerHandler("r", new ShellCommandHandler());
	}
	
	private void processArguments() {
		BufferWindow window = (BufferWindow)_primaryWindow;
		if (_argv.length == 1) {
			String path = _argv[0];
			File file = FileUtil.getFile(path);
			try {
				window.openFile(file);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	public void start(String[] argv) {
		_argv = argv;
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
					ServiceManager sm = ServiceManager.getInstance();
					IConsoleService cs = sm.getConsoleService();
					cs.getCursorService().changeCursor(ICursorService.CURSOR_BLOCK);
					cs.deactivate();
					if (_exception != null) {
						_exception.printStackTrace();
					}
			    }
			});

			setupCommands();
			_loop = new EventLoop();
			ServiceManager sm = ServiceManager.getInstance();
			IConsoleService cs = sm.getConsoleService();
			
			cs.activate();
			
			Rectangle windowRect = new Rectangle(0, 0, cs.getScreenWidth(), cs.getScreenHeight());
			
			_primaryWindow = new BufferWindow(windowRect);
			processArguments();

			_loop.setPrimaryResponder(_primaryWindow);
			
			Log.println("Initializing app with window: [" + cs.getScreenWidth() + ", " + cs.getScreenHeight() + "]");

			_primaryWindow.draw();

			_loop.start();	
		} catch (Throwable throwable) {
			Application app = Application.getApplication();
			app.setException(throwable);
			app.exit();
		}
	}
	
	public void setException(Throwable throwable) {
		_exception = throwable;
	}
	
	public void exit() {
		System.exit(0);
	}
}
