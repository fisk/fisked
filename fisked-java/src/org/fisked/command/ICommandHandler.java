package org.fisked.command;

import org.fisked.buffer.BufferWindow;

public interface ICommandHandler {
	void run(BufferWindow window, String[] argv);
}
