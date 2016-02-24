package org.fisked.command.provided;

import java.util.concurrent.Future;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.IFiskedCoreService;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.OpenFileCommand;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandManager;
import org.fisked.shell.ShellCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
public class ApplicationCommands {
	private final static Logger LOG = LoggerFactory.getLogger(ApplicationCommands.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(ApplicationCommands.class);

	private CommandHandlerReference _quitCommand;
	private CommandHandlerReference _openCommand;
	private CommandHandlerReference _saveCommand;
	private CommandHandlerReference _shellCommand;

	@Validate
	public void start() {
		try (IBehaviorConnection<ICommandManager> commandBC = BEHAVIORS.getBehaviorConnection(ICommandManager.class)
				.get()) {
			LOG.debug("Registering application commands.");
			ICommandManager cm = commandBC.getBehavior();

			_quitCommand = cm.registerHandler("q", (BufferWindow window, String[] argv) -> {
				try (IBehaviorConnection<IFiskedCoreService> applicationBC = BEHAVIORS
						.getBehaviorConnection(IFiskedCoreService.class).get()) {
					applicationBC.getBehavior().exit(0);
				} catch (Exception e) {
					LOG.error("Couldn't shut down gracefully.");
					System.exit(-1);
				}
			});
			_openCommand = cm.registerHandler("e", new OpenFileCommand());
			_saveCommand = cm.registerHandler("w", (BufferWindow window, String[] argv) -> {
				try {
					window.getBuffer().save();
					window.getCommandController().setCommandFeedback("Saved file.");
				} catch (Exception e) {
					window.getCommandController().setCommandFeedback("Couldn't save.");
				}
			});
			_shellCommand = cm.registerHandler("r", new ShellCommandHandler());
		} catch (Exception e) {
			LOG.error("Couldn't start application commands: ", e);
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
			LOG.debug("Unregistering application commands.");
			ICommandManager cm = commandBC.getBehavior();

			cm.removeHandler(_quitCommand);
			cm.removeHandler(_openCommand);
			cm.removeHandler(_saveCommand);
			cm.removeHandler(_shellCommand);
		} catch (Exception e) {
			LOG.error("Couldn't stop application commands: ", e);
		}
	}

}
