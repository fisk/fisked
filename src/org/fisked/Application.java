package org.fisked;

import org.fisked.buffer.Window;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.responder.EventLoop;

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
	
	public void start() {
		_loop = new EventLoop();
		Rectangle windowRect = new Rectangle(0, 0, getScreenWidth(), getScreenHeight());
		_primaryWindow = new Window(windowRect);
		
		_loop.setPrimaryResponder(_primaryWindow);
		
		Toolkit.init();
		
		_primaryWindow.draw();
		
		_loop.start();
	}
	
	public int getScreenWidth() {
		return Toolkit.getScreenWidth();
	}
	
	public int getScreenHeight() {
		return Toolkit.getScreenHeight();
	}
	
	public void exit() {
		Toolkit.shutdown();
		System.exit(0);
	}
}
