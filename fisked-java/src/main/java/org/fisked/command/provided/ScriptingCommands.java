/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
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
package org.fisked.command.provided;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.buffer.controller.FatTextSelection;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandManager;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.VisualMode;
import org.fisked.util.FileUtil;
import org.fisked.util.Wrapper;
import org.fisked.util.models.Range;
import org.fisked.util.models.selection.SelectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jline.internal.Log;

@Component(immediate = true, publicFactory = false)
@Instantiate
public class ScriptingCommands {
	private final static Logger LOG = LoggerFactory.getLogger(ScriptingCommands.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(ScriptingCommands.class);

	private void evaluateScript(BufferWindow window, String language) {
		AbstractMode mode = window.getCurrentMode();
		if (!(mode instanceof VisualMode)) {
			LOG.error("Called evaluate script from outside visual mode.");
			window.getCommandController().setCommandFeedback("Called evaluate script from outside visual mode.");
			return;
		}
		VisualMode visualMode = (VisualMode) mode;
		if (visualMode.getMode() == SelectionMode.BLOCK_MODE) {
			LOG.debug("Not yet supported");
			window.getCommandController().setCommandFeedback("Can't evaluate scripts in visual block mode.");
			return;
		}
		Buffer buffer = window.getBuffer();
		Wrapper<Integer> lastIndex = new Wrapper<>();

		try (UndoScope us = buffer.createUndoScope()) {
			List<FatTextSelection> list = window.getBufferController().getFatTextSelections();

			for (int i = list.size() - 1; i >= 0; i--) {
				FatTextSelection selection = list.get(i);

				LOG.debug("Evaluating script of type: " + language);
				String text = selection.getText();
				try (IBehaviorConnection<ISourceEvaluatorManager> managerBC = BEHAVIORS
						.getBehaviorConnection(ISourceEvaluatorManager.class).get()) {
					managerBC.getBehavior().getEvaluator(language, (evaluator) -> {
						LOG.debug("Running script:\n" + text);
						String result;
						try {
							result = evaluator.evaluate(text);
							Log.debug("Result: " + result);
						} catch (Throwable e) {
							result = e.getMessage();
						}
						if (result != null) {
							Range selectionRange = selection.getRanges().get(0);
							int startIndex = selectionRange.getStart();
							buffer.removeCharsInRangeLogged(selectionRange);
							buffer.insertString(startIndex, result);
							lastIndex.setValue(selectionRange.getStart());
							window.switchToNormalMode();
						} else {
							LOG.debug("Evaluator did not reply.");
							window.getCommandController().setCommandFeedback("Evaluator did not reply.");
						}
					});
				} catch (Exception e) {
					LOG.error("Can't evaluate source: ", e);
				}
			}

			window.getBufferController().collapseCursors(lastIndex.getValue());
			window.getCommandController().setCommandFeedback("Can't evaluate script without selection.");
		}
	}

	private CommandHandlerReference _rubyCommand;
	private CommandHandlerReference _pythonCommand;
	private CommandHandlerReference _javascriptCommand;
	private CommandHandlerReference _lispCommand;
	private CommandHandlerReference _groovyCommand;
	private CommandHandlerReference _scriptCommand;

	@Validate
	public void start() {
		try (IBehaviorConnection<ICommandManager> commandBC = BEHAVIORS.getBehaviorConnection(ICommandManager.class)
				.get()) {
			LOG.debug("Registering scripting commands.");
			ICommandManager cm = commandBC.getBehavior();

			_rubyCommand = cm.registerHandler("ruby", (BufferWindow window, String[] argv) -> {
				evaluateScript(window, "ruby");
			});
			_pythonCommand = cm.registerHandler("python", (BufferWindow window, String[] argv) -> {
				evaluateScript(window, "python");
			});
			_javascriptCommand = cm.registerHandler("javascript", (BufferWindow window, String[] argv) -> {
				evaluateScript(window, "javascript");
			});
			_lispCommand = cm.registerHandler("lisp", (BufferWindow window, String[] argv) -> {
				evaluateScript(window, "lisp");
			});
			_groovyCommand = cm.registerHandler("groovy", (BufferWindow window, String[] argv) -> {
				evaluateScript(window, "groovy");
			});
			_scriptCommand = cm.registerHandler("script", (BufferWindow window, String[] argv) -> {
				if (argv.length != 2)
					return;
				File file = FileUtil.getFile(argv[1]);

				try (IBehaviorConnection<ISourceEvaluatorManager> managerBC = BEHAVIORS
						.getBehaviorConnection(ISourceEvaluatorManager.class).get()) {
					managerBC.getBehavior().getEvaluator(file, (evaluator) -> {
						String string;
						try {
							string = FileUtils.readFileToString(file);
							string = evaluator.evaluate(string);
						} catch (Exception e) {
							string = e.getMessage();
						}
						window.getBufferController().getBuffer().appendStringAtPointLogged(string);
						window.switchToNormalMode();
					});
				} catch (Exception e) {
					LOG.error("Can't evaluate source: ", e);
				}
			});
		} catch (Exception e) {
			LOG.error("Couldn't start scripting commands: ", e);
		}
	}

	@Invalidate
	public void stop() {
		Future<IBehaviorConnection<ICommandManager>> commandBCF = BEHAVIORS
				.getBehaviorConnection(ICommandManager.class);
		if (!commandBCF.isDone()) {
			LOG.debug("Could not unregister as registering ");
			return;
		}
		try (IBehaviorConnection<ICommandManager> commandBC = commandBCF.get()) {
			LOG.debug("Unregistering scripting commands.");
			ICommandManager cm = commandBC.getBehavior();

			cm.removeHandler(_rubyCommand);
			cm.removeHandler(_pythonCommand);
			cm.removeHandler(_javascriptCommand);
			cm.removeHandler(_lispCommand);
			cm.removeHandler(_groovyCommand);
			cm.removeHandler(_scriptCommand);
		} catch (Exception e) {
			LOG.error("Couldn't stop scripting commands: ", e);
		}
	}
}
