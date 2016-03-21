package org.fisked.email.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.IApplication;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandManager;
import org.fisked.email.service.Email;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailProfileVendor;
import org.fisked.email.service.IEmailSendService;
import org.fisked.email.service.IEmailSendService.IEmailSendCallback;
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
	private CommandHandlerReference _sendCommand;

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

					try (IBehaviorConnection<IApplication> applicationBC = BEHAVIORS
							.getBehaviorConnection(IApplication.class).get()) {
						LOG.debug("Activating email window.");
						applicationBC.getBehavior().pushPrimaryWindow(emailWindow);
						emailWindow.getController().start();
					}
				} catch (Exception e) {
					LOG.error("Can't get console service: ", e);
				}

			});
			_sendCommand = cm.registerHandler("send", (BufferWindow window, String[] argv) -> {
				try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS
						.getBehaviorConnection(IConsoleService.class).get()) {
					IConsoleService cs = consoleBC.getBehavior();

					if (argv.length == 1) {
						Buffer buffer = window.getBuffer();
						String emailString = buffer.toString();
						sendEmailString(emailString);
					} else if (argv.length == 2 || argv.length == 3) {
						String from;
						String to = argv[1];

						if (argv.length == 2) {
							try (IBehaviorConnection<IEmailProfileVendor> vendorBC = BEHAVIORS
									.getBehaviorConnection(IEmailProfileVendor.class).get()) {
								LOG.debug("Starting email window.");
								EmailProfile[] profiles = vendorBC.getBehavior().getEmailProfiles();
								if (profiles.length == 1) {
									from = profiles[0].getEmail();
								} else {
									LOG.debug("Couldn't determine sender.");
									return;
								}
							}
						} else {
							from = argv[2];
						}

						Rectangle windowRect = new Rectangle(0, 0, cs.getScreenWidth(), cs.getScreenHeight());
						Buffer buffer = new Buffer("To: " + to + "\nFrom: " + from + "\nSubject: " + "\n\n");
						BufferWindow emailWindow = new BufferWindow(windowRect);
						emailWindow.setBuffer(buffer);

						try (IBehaviorConnection<IApplication> applicationBC = BEHAVIORS
								.getBehaviorConnection(IApplication.class).get()) {
							LOG.debug("Starting email window.");
							applicationBC.getBehavior().pushPrimaryWindow(emailWindow);
						}
					} else {
						LOG.debug("Wrong number or arguments");
					}

				} catch (Exception e) {
					LOG.error("Can't get console service: ", e);
				}

			});
		} catch (Exception e) {
			LOG.error("Couldn't start email commands: ", e);
		}
	}

	final Pattern _headerPattern = Pattern.compile("([^:]+): (.*)");

	private void sendEmailString(String emailString) {
		try (Scanner scanner = new Scanner(emailString)) {
			Map<String, String> headers = new HashMap<>();
			String line = scanner.nextLine();
			while (!line.isEmpty()) {
				Matcher match = _headerPattern.matcher(line);
				if (!match.matches()) {
					LOG.debug("Wrong format of headers.");
					return;
				} else {
					String key = match.group(1);
					String value = MimeUtility.encodeText(match.group(2));
					headers.put(key, value);
					LOG.debug("Email header: " + key + ": " + value);
				}
				line = scanner.nextLine();
			}
			StringBuilder body = new StringBuilder();
			while (scanner.hasNextLine()) {
				body.append(scanner.nextLine());
			}
			String bodyStr = body.toString();
			String from = headers.get("From");
			String to = headers.get("To");
			String cc = headers.get("Cc");
			String subject = headers.get("Subject");
			if (from == null) {
				LOG.debug("No sender.");
				return;
			}
			if (to == null) {
				LOG.debug("No receiver");
			}
			try (IBehaviorConnection<IEmailProfileVendor> vendorBC = BEHAVIORS
					.getBehaviorConnection(IEmailProfileVendor.class).get()) {
				LOG.debug("Getting email profile.");
				EmailProfile profile = vendorBC.getBehavior().getEmailProfileByEmail(from);
				if (profile == null) {
					throw new RuntimeException("Couldn't get sender profile.");
				}
				try (IBehaviorConnection<IEmailSendService> senderBC = BEHAVIORS
						.getBehaviorConnection(IEmailSendService.class).get()) {
					Email email = new Email();
					email.setBody(bodyStr);
					email.setFrom(from);
					email.setSubject(subject);
					if (to != null) {
						String[] toSubArr = to.split(";");
						for (String toSubstring : toSubArr) {
							email.addReceiver(toSubstring);
						}
					}
					if (cc != null) {
						String[] ccSubArr = cc.split(";");
						for (String ccSubstring : ccSubArr) {
							email.addCc(ccSubstring);
						}
					}
					senderBC.getBehavior().sendEmail(email, profile, new IEmailSendCallback() {

						@Override
						public void success(Email email, EmailProfile profile) {
							LOG.debug("Sent message successfully.");
						}

						@Override
						public void error(Email email, EmailProfile profile, Exception e) {
							LOG.debug("Couldn't send email: ", e);
						}

					});
				}
			}
		} catch (Exception e) {
			LOG.debug("Couldn't send email: ", e);
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
			LOG.debug("Unregistering emailing commands.");
			ICommandManager cm = commandBC.getBehavior();

			cm.removeHandler(_emailCommand);
			cm.removeHandler(_sendCommand);
		} catch (Exception e) {
			LOG.error("Couldn't stop email commands: ", e);
		}
	}
}
