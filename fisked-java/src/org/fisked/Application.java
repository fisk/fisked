package org.fisked;

import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.Window;
import org.fisked.command.CommandManager;
import org.fisked.command.OpenFileCommand;
import org.fisked.responder.EventLoop;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;

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
		
	}
	
	public void start() {
		try {
			Toolkit.init();
			Toolkit.setEncoding("UTF-8");

			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
					Toolkit.shutdown();
					if (_exception != null) {
						_exception.printStackTrace();
					}
			    }
			});
			
			setupCommands();
			_loop = new EventLoop();
			
			Rectangle windowRect = new Rectangle(0, 0, getScreenWidth(), getScreenHeight());
			
			_primaryWindow = new BufferWindow(windowRect);

			_loop.setPrimaryResponder(_primaryWindow);

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
	
	public int getScreenWidth() {
		return Toolkit.getScreenWidth();
	}
	
	public int getScreenHeight() {
		return Toolkit.getScreenHeight();
	}
	
	public void exit() {
		System.exit(0);
	}
}
