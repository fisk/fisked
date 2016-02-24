package org.fiskedemail.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.Folder;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.drawing.Window;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailFetchService;
import org.fisked.email.service.IEmailFetchService.IEmailFetchFoldersCallback;
import org.fisked.email.service.IEmailProfileVendor;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.util.concurrency.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailWindow extends Window {
	private final static Logger LOG = LoggerFactory.getLogger(EmailWindow.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(EmailWindow.class);

	public EmailWindow(Rectangle windowRect) {
		super(windowRect);
	}

	private class Account {
		private final EmailProfile _profile;
		private final Folder[] _folders;

		public Account(EmailProfile profile, Folder[] folders) {
			_profile = profile;
			_folders = folders;
		}
	}

	private final List<Account> _accounts = Collections.synchronizedList(new ArrayList<>());

	public void getEmailAccounts() {
		try (IBehaviorConnection<IEmailProfileVendor> profileBC = BEHAVIORS
				.getBehaviorConnection(IEmailProfileVendor.class).get()) {
			EmailProfile[] profiles = profileBC.getBehavior().getEmailProfiles();
			for (EmailProfile profile : profiles) {
				getEmailFolders(profile);
			}
		} catch (Exception e) {
			LOG.error("Could not get email profile: ", e);
		}
	}

	public void getEmailFolders(EmailProfile profile) {
		try (IBehaviorConnection<IEmailFetchService> fetchBC = BEHAVIORS.getBehaviorConnection(IEmailFetchService.class)
				.get()) {
			fetchBC.getBehavior().getFolders(profile, new IEmailFetchFoldersCallback() {

				@Override
				public void success(Folder[] folders) {
					_accounts.add(new Account(profile, folders));
					Dispatcher.getInstance().runMain(() -> {
						setNeedsFullRedraw();
						refresh();
					});
				}

				@Override
				public void error(EmailProfile profile, Exception e) {
					_accounts.clear();
					Dispatcher.getInstance().runMain(() -> {
						setNeedsFullRedraw();
						refresh();
					});
				}

			});
		} catch (Exception e) {
			LOG.error("Could not get email profile: ", e);
		}
	}

}
