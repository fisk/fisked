package org.fisked.services;

import java.io.File;

import javax.mail.Folder;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.email.service.Email;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailFetchService;
import org.fisked.email.service.IEmailFetchService.IEmailFetchFolderCallback;
import org.fisked.email.service.IEmailFetchService.IEmailFetchFoldersCallback;
import org.fisked.email.service.IEmailProfileVendor;
import org.fisked.email.service.IEmailSendService;
import org.fisked.email.service.IEmailSendService.IEmailSendCallback;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.launcher.service.ILauncherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = ComponentManager.COMPONENT_NAME)
public class ComponentManager {
	public static final String COMPONENT_NAME = "ComponentManager";
	private volatile static ComponentManager _singleton;
	private final static Logger LOG = LoggerFactory.getLogger(ComponentManager.class);

	@Requires
	private ISourceEvaluatorManager _sourceEvalManager;

	@Requires
	private ILauncherService _launcherService;

	@Requires(filter = IEmailSendService.DYNAMICALLY_FIND_PROTOCOL_FILTER)
	private IEmailSendService _emailSend;

	@Requires(filter = IEmailFetchService.DYNAMICALLY_FIND_PROTOCOL_FILTER)
	private IEmailFetchService _emailFetch;

	@Requires
	private IEmailProfileVendor _profileVendor;

	public void sendEmail() {
		Email email = new Email();
		email.setBody("Test email.");
		email.setFrom("fisksvett@gmail.com");
		email.setSubject("Testing");
		email.addReceiver("fisksvett@gmail.com");
		email.addAttachment(new File("/tmp/fisked.log"));
		EmailProfile profile = _profileVendor.getEmailProfileByEmail(email.getFrom());
		_emailSend.sendEmail(email, profile, new IEmailSendCallback() {

			@Override
			public void success(Email email, EmailProfile profile) {
				LOG.debug("Sent an email to " + email.getReceivers().get(0));
			}

			@Override
			public void error(Email email, EmailProfile profile, Exception e) {
				LOG.error("Could not send email to " + email.getReceivers().get(0) + ": ", e);
			}

		});

		_emailFetch.getFolder(profile, "inbox", new IEmailFetchFolderCallback() {

			@Override
			public void success(Folder folder) {
				LOG.debug("Got email box: " + folder.getFullName());
			}

			@Override
			public void error(EmailProfile profile, String name, Exception e) {
				LOG.error("Could not fetch email box " + name + ": ", e);
			}

		});

		_emailFetch.getFolders(profile, new IEmailFetchFoldersCallback() {

			@Override
			public void success(Folder[] folders) {
				LOG.debug("Got email boxes:");
				for (Folder folder : folders) {
					LOG.debug("\t" + folder.getFullName());
				}
			}

			@Override
			public void error(EmailProfile profile, Exception e) {
				LOG.error("Could not fetch email boxes: ", e);
			}

		});
	}

	@Validate
	public void start() {
		_singleton = this;
	}

	@Invalidate
	public void stop() {
		_singleton = null;
	}

	public static ComponentManager getInstance() {
		return _singleton;
	}

	public ISourceEvaluatorManager getSourceEvalManager() {
		return _sourceEvalManager;
	}

	public ILauncherService getLauncherService() {
		return _launcherService;
	}
}
