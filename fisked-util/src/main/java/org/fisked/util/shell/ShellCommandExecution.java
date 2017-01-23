/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.util.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;

import org.fisked.util.concurrency.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellCommandExecution {
	private final static Logger LOG = LoggerFactory.getLogger(ShellCommandExecution.class);

	private final String[] _command;
	private final ProcessBuilder _processBuilder;
	private String _inputString;

	public void setInputString(String string) {
		_inputString = string;
	}

	public void redirectInput() {
		_processBuilder.redirectInput(Redirect.INHERIT);
	}

	public ShellCommandExecution(String... command) {
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
			LOG.error("Could not run command: ", e);
			return new ExecutionResult("", -1);
		}

		StringBuilder result = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;

			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			status = process.exitValue();
			LOG.debug(String.join(" ", _command));
			LOG.debug(result.toString());
			return new ExecutionResult(result.toString(), status);
		} catch (Throwable e) {
			LOG.error("Could interpret command output: ", e);
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
