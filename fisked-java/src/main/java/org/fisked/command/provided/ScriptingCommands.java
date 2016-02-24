package org.fisked.command.provided;

import java.io.File;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandManager;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jline.internal.Log;

@Component(immediate = true, publicFactory = false)
@Instantiate
public class ScriptingCommands {
	private final static Logger LOG = LoggerFactory.getLogger(ScriptingCommands.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(ScriptingCommands.class);

	private void evaluateScript(BufferWindow window, String language) {
		if (window.getBufferController().getSelection() != null) {
			LOG.debug("Evaluating script of type: " + language);
			String text = window.getBufferController().getSelectedText();
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
						window.getBufferController().setSelectionText(result);
						window.switchToNormalMode();
					} else {
						LOG.debug("Evaluator did not reply.");
						window.getCommandController().setCommandFeedback("Evaluator did not reply.");
					}
				});
			} catch (Exception e) {
				LOG.error("Can't evaluate source: ", e);
			}
		} else {
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
