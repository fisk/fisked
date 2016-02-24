package org.fisked.command.api;

import org.fisked.buffer.BufferWindow;

public interface ICommandManager {
	CommandHandlerReference registerHandler(String command, ICommandHandler handler);

	void removeHandler(CommandHandlerReference handler);

	boolean handle(BufferWindow window, String command, String[] argv);
}
