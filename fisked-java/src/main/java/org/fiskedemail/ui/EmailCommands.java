package org.fiskedemail.ui;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandManager;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
public class EmailCommands {
	private final static Logger LOG = LoggerFactory.getLogger(EmailCommands.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(EmailCommands.class);

	private CommandHandlerReference _emailCommand;

	@Validate
	public void start() {
		try (IBehaviorConnection<ICommandManager> commandBC = BEHAVIORS.getBehaviorConnection(ICommandManager.class)
				.get()) {
			LOG.debug("Registering email commands.");
			ICommandManager cm = commandBC.getBehavior();

			_emailCommand = cm.registerHandler("email", (BufferWindow window, String[] argv) -> {
				try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS
						.getBehaviorConnection(IConsoleService.class).get()) {
					IConsoleService cs = consoleBC.getBehavior();

					Rectangle windowRect = new Rectangle(0, 0, cs.getScreenWidth(), cs.getScreenHeight());
					EmailWindow emailWindow = new EmailWindow(windowRect);

				} catch (Exception e) {
					LOG.error("Can't get console service: ", e);
				}

			});
		} catch (Exception e) {
			LOG.error("Couldn't start application commands: ", e);
		}
	}

	@Invalidate
	public void stop() {
	}
}
