package org.fisked.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.fisked.util.concurrency.Dispatcher;

public class ShellCommandExecution {
	private final String _command;

	public ShellCommandExecution(String command) {
		_command = command;
	}

	interface CommandResult {
		void call(String string);
		void finished(int status);
	}

	void execute(CommandResult callback) {
		final CommandResult cb = callback;
		executeIncremental(new CommandResult() {
			StringBuilder _string = new StringBuilder();

			@Override
			public void call(String line) {
				_string.append(line);
				_string.append("\n");
			}

			@Override
			public void finished(int status) {
				cb.call(_string.toString());
				cb.finished(status);
			}
			
		});
	}

	void executeIncremental(CommandResult callback) {
		Dispatcher dispatcher = Dispatcher.getInstance();
		dispatcher.runConc(() -> {
			Runtime runtime = Runtime.getRuntime();
			Process process;
			int status;
			try {
				process = runtime.exec(_command);
			} catch (Exception e1) {
				callback.finished(-1);
				return;
			}
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				
				while ((line = reader.readLine()) != null) {
					callback.call(line);
				}
				status = process.exitValue();
				callback.finished(status);
			} catch (Throwable e) {
				callback.finished(-1);
			}
		});

	}
}
