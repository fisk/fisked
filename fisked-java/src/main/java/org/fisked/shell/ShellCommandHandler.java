package org.fisked.shell;

import java.util.Arrays;

import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.ICommandHandler;
import org.fisked.util.shell.ShellCommandExecution;
import org.fisked.util.shell.ShellCommandExecution.CommandResult;

public class ShellCommandHandler implements ICommandHandler {

	@Override
	public void run(BufferWindow window, String[] argv) {
		String command = String.join(" ", Arrays.copyOfRange(argv, 1, argv.length));
		command = "bash -c " + command;

		ShellCommandExecution execution = new ShellCommandExecution(command);
		execution.executeAsyncIncremental(new CommandResult() {

			@Override
			public void call(String string) {
				window.getBuffer().appendStringAtPointLogged(string);
				window.refresh();
			}

			@Override
			public void finished(int status) {
				window.getCommandController().setCommandFeedback("Process exited: " + status);
			}

		});
	}

}
