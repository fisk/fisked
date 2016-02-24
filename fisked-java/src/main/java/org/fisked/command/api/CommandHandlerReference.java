package org.fisked.command.api;

import org.fisked.buffer.BufferWindow;

public class CommandHandlerReference implements ICommandHandler {
	private final String _command;
	private final ICommandHandler _handler;

	public CommandHandlerReference(String command, ICommandHandler handler) {
		_command = command;
		_handler = handler;
	}

	@Override
	public void run(BufferWindow window, String[] argv) {
		_handler.run(window, argv);
	}

	public String getCommand() {
		return _command;
	}

	public ICommandHandler getHandler() {
		return _handler;
	}
}
