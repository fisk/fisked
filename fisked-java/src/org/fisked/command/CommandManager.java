package org.fisked.command;

import java.util.HashMap;
import java.util.Map;

import org.fisked.buffer.BufferWindow;

public class CommandManager {
	private static CommandManager _sharedInstance;
	public static CommandManager getSingleton() {
		if (_sharedInstance != null) return _sharedInstance;
		synchronized (CommandManager.class) {
			if (_sharedInstance == null) {
				_sharedInstance = new CommandManager();
			}
		}
		return _sharedInstance;
	}

	private Map<String, ICommandHandler> _map = new HashMap<>();
	
	public void registerHandler(String command, ICommandHandler handler) {
		_map.put(command, handler);
	}
	
	public boolean handle(BufferWindow window, String command, String[] argv) {
		ICommandHandler handler = _map.get(command);
		if (handler == null) {
			return false;
		}
		handler.run(window, argv);
		return true;
	}
}
