package org.fisked.services;

import org.fisked.Application;
import org.fisked.renderingengine.ClipboardService;
import org.fisked.renderingengine.ConsoleService;
import org.fisked.renderingengine.MacClipboardService;
import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.util.OSDetector;

public class ServiceManager {
	private static ServiceManager _sharedInstance;
	public static ServiceManager getInstance() {
		if (_sharedInstance != null) return _sharedInstance;
		synchronized (Application.class) {
			if (_sharedInstance == null) {
				_sharedInstance = new ServiceManager();
			}
		}
		return _sharedInstance;
	}
	
	private IConsoleService _consoleService = new ConsoleService();
	private IClipboardService _clipboardService = new ClipboardService();
	
	public ServiceManager() {
		if (OSDetector.isMac()) {
			_clipboardService = new MacClipboardService();
		}
	}
	
	public IClipboardService getClipboardService() {
		return _clipboardService;
	}
	
	public IConsoleService getConsoleService() {
		return _consoleService;
	}
}
