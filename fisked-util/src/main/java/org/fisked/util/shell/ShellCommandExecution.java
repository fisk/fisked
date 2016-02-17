package org.fisked.util.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.fisked.util.concurrency.Dispatcher;

public class ShellCommandExecution {
	private final String _command;
	private final ProcessBuilder _processBuilder;
	private String _inputString;

	public void setInputSource(String string) {
		_inputString = string;
	}

	public ShellCommandExecution(String command) {
		_command = command;
		_processBuilder = new ProcessBuilder(_command);
		_processBuilder.redirectErrorStream(true);
	}

	private void feedInput(Process process) throws IOException {
		if (_inputString != null) {
			OutputStream stdin = process.getOutputStream();
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin))) {
				writer.write(_inputString);
				writer.flush();
			}
		}
	}

	public interface CommandResult {
		void call(String string);

		void finished(int status);
	}

	void executeAsync(CommandResult callback) {
		final CommandResult cb = callback;
		executeAsyncIncremental(new CommandResult() {
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

	public class ExecutionResult {
		private final int _status;
		private final String _result;

		public ExecutionResult(String result, int status) {
			_status = status;
			_result = result;
		}

		public int getStatus() {
			return _status;
		}

		public String getResult() {
			return _result;
		}
	}

	public ExecutionResult executeSync() {
		int status;

		Process process;
		try {
			process = _processBuilder.start();
			feedInput(process);
			process.waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		StringBuilder result = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;

			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			status = process.exitValue();
			return new ExecutionResult(result.toString(), status);
		} catch (Throwable e) {
			return new ExecutionResult("", -1);
		}
	}

	public void executeAsyncIncremental(CommandResult callback) {
		Dispatcher dispatcher = Dispatcher.getInstance();
		dispatcher.runConc(() -> {
			int status;
			Process process;
			try {
				process = _processBuilder.start();
				feedInput(process);
				process.waitFor();
			} catch (Exception e) {
				throw new RuntimeException(e);
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
