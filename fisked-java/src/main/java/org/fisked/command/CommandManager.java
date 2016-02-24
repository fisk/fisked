package org.fisked.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandHandler;
import org.fisked.command.api.ICommandManager;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class CommandManager implements ICommandManager {
	private final Map<String, CommandHandlerReference> _map = new ConcurrentHashMap<>();

	@Override
	public CommandHandlerReference registerHandler(String command, ICommandHandler handler) {
		CommandHandlerReference handlerWrapper = new CommandHandlerReference(command, handler);
		_map.put(command, handlerWrapper);
		return handlerWrapper;
	}

	@Override
	public boolean handle(BufferWindow window, String command, String[] argv) {
		ICommandHandler handler = _map.get(command);
		if (handler == null) {
			return false;
		}
		handler.run(window, argv);
		return true;
	}

	@Override
	public void removeHandler(CommandHandlerReference handler) {
		_map.remove(handler.getCommand(), handler);
	}
}
