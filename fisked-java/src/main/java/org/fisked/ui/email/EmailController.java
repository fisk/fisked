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
package org.fisked.ui.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeUtility;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.Buffer;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailFetchService;
import org.fisked.email.service.IEmailFetchService.IEmailFetchFoldersCallback;
import org.fisked.email.service.IEmailProfileVendor;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.ui.listview.ListView;
import org.fisked.ui.listview.ListView.ListViewDataSource;
import org.fisked.ui.listview.ListView.ListViewDelegate;
import org.fisked.ui.screen.Screen;
import org.fisked.ui.window.IWindowManager;
import org.fisked.util.concurrency.Dispatcher;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Color;
import org.fisked.util.models.Rectangle;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailController {
	private final static Logger LOG = LoggerFactory.getLogger(EmailController.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(EmailController.class);
	private final EmailWindow _window;

	private final List<Account> _accounts = Collections.synchronizedList(new ArrayList<>());

	public EmailController(EmailWindow window) {
		_window = window;
	}

	public void start() {
		getEmailAccounts();
	}

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
					Account account = new Account(profile, folders);
					_accounts.add(account);
					setupAccountListView();
					Dispatcher.getInstance().runMain(() -> {
						_window.setNeedsFullRedraw();
						_window.refresh();
					});
				}

				@Override
				public void error(EmailProfile profile, Exception e) {
					_accounts.clear();
					Dispatcher.getInstance().runMain(() -> {
						_window.setNeedsFullRedraw();
						_window.refresh();
					});
				}

			});
		} catch (Exception e) {
			LOG.error("Could not get email profile: ", e);
		}
	}

	private void setupAccountListView() {
		LOG.debug("Setting up account list view.");
		Rectangle windowRect = _window.getRootView().getClippingRect();
		Rectangle listViewRect = new Rectangle(0, 0, windowRect.getSize().getWidth(), windowRect.getSize().getHeight());
		ListView<Account> listView = new ListView<>(listViewRect);
		listView.setDataSource(new ListViewDataSource<Account>() {

			@Override
			public int length() {
				return _accounts.size();
			}

			@Override
			public Account get(int index) {
				return _accounts.get(index);
			}

		});
		listView.setDelegate(new ListViewDelegate<Account>() {

			@Override
			public int getElementLines() {
				return 1;
			}

			@Override
			public AttributedString toString(Account element, boolean selected) {
				String title = "" + element.getProfile().getName() + " (" + element.getProfile().getEmail() + ")";
				AttributedString result = new AttributedString(title);
				if (selected) {
					result.setBackgroundColor(Color.BLUE);
					result.setForegroundColor(Color.BLACK);
				} else {
					result.setBackgroundColor(Color.NORMAL);
					result.setForegroundColor(Color.BLUE);
				}
				return result;
			}

			@Override
			public void selectedItem(int index) {
				Account account = _accounts.get(index);
				setupFolderListView(account);
			}

		});
		_window.setRootView(listView);
		_window.pushResponderChain(listView.createResponder());
		_window.setNeedsFullRedraw();
		_window.refresh();
	}

	private void setupFolderListView(Account account) {
		LOG.debug("Setting up email list view.");
		Rectangle windowRect = _window.getRootView().getClippingRect();
		Rectangle listViewRect = new Rectangle(0, 0, windowRect.getSize().getWidth(), windowRect.getSize().getHeight());
		ListView<Folder> listView = new ListView<>(listViewRect);
		listView.setDataSource(new ListViewDataSource<Folder>() {

			@Override
			public int length() {
				return account.getFolders().length;
			}

			@Override
			public Folder get(int index) {
				return account.getFolders()[index];
			}

		});
		listView.setDelegate(new ListViewDelegate<Folder>() {

			@Override
			public int getElementLines() {
				return 1;
			}

			@Override
			public AttributedString toString(Folder element, boolean selected) {
				String title = element.getFullName();
				AttributedString result = new AttributedString(title);
				if (selected) {
					result.setBackgroundColor(Color.BLUE);
					result.setForegroundColor(Color.BLACK);
				} else {
					result.setBackgroundColor(Color.NORMAL);
					result.setForegroundColor(Color.BLUE);
				}
				return result;
			}

			@Override
			public void selectedItem(int index) {
				Folder folder = account.getFolders()[index];
				setupFolder(folder);
			}

		});
		_window.setRootView(listView);
		_window.pushResponderChain(listView.createResponder());
		_window.setNeedsFullRedraw();
		_window.refresh();
	}

	public void setupFolder(Folder folder) {
		LOG.debug("Setting up email folder view.");

		try {
			folder.open(Folder.READ_ONLY);
		} catch (MessagingException e1) {
			LOG.debug("Could not open folder: ", e1);
			return;
		}

		Rectangle windowRect = _window.getRootView().getClippingRect();
		Rectangle listViewRect = new Rectangle(0, 0, windowRect.getSize().getWidth(), windowRect.getSize().getHeight());
		ListView<Message> listView = new ListView<>(listViewRect);

		ListViewDataSource<Message> dataSource = new ListViewDataSource<Message>() {

			@Override
			public int length() {
				try {
					return folder.getMessageCount();
				} catch (MessagingException e) {
					return 0;
				}
			}

			@Override
			public Message get(int index) {
				index = length() - index;
				try {
					return folder.getMessage(index);
				} catch (MessagingException e) {
					return null;
				}
			}

		};
		listView.setDataSource(dataSource);
		listView.setDelegate(new ListViewDelegate<Message>() {

			@Override
			public int getElementLines() {
				return 1;
			}

			@Override
			public void selectedItem(int index) {
				LOG.debug("Selected item: " + index);
				StringBuilder stringBuilder = new StringBuilder();
				Message message = dataSource.get(index);

				ClassLoader tcl = Thread.currentThread().getContextClassLoader();

				try {
					Thread.currentThread().setContextClassLoader(javax.mail.Session.class.getClassLoader());

					// Get headers
					String[] headerKeys = { "Delivered-To", "Date", "From", "Subject", "Cc", "Reply-To" };
					Set<String> headerKeySet = new HashSet<>();
					for (String key : headerKeys) {
						headerKeySet.add(key);
					}
					@SuppressWarnings("unchecked")
					Enumeration<Header> headers = message.getAllHeaders();
					while (headers.hasMoreElements()) {
						Header header = headers.nextElement();
						if (headerKeySet.contains(header.getName())) {
							String valueUTF8 = MimeUtility.decodeText(header.getValue());
							stringBuilder.append(header.getName() + ": " + valueUTF8 + "\n");
						}
					}

					// Get content
					stringBuilder.append("\n");
					Object content = message.getContent();
					if (message.isMimeType("text/plain")) {
						stringBuilder.append(content.toString());
					} else if (message.isMimeType("multipart/*")) {
						Multipart mp = (Multipart) content;
						int count = mp.getCount();
						for (int i = 0; i < count; i++) {
							BodyPart part = mp.getBodyPart(i);
							if (part.isMimeType("text/plain")) {
								stringBuilder.append(part.getContent());
							} else if (part.isMimeType("text/html")) {
								String html = (String) part.getContent();
								String plaintext = Jsoup.parse(html).text();
								stringBuilder.append(plaintext);
							}
						}
					}

					Buffer buffer = new Buffer(stringBuilder.toString());
					BufferWindow window = new BufferWindow(windowRect, "Email");
					window.setBuffer(buffer);

					try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS
							.getBehaviorConnection(IWindowManager.class).get()) {
						LOG.debug("Activating email window.");
						Screen screen = new Screen("Email Screen");
						screen.attachWindow(window);
						wmBC.getBehavior().pushPrimaryScreen(screen);
					}
				} catch (Exception e) {
					LOG.debug("Couldn't read email: ", e);
				} finally {
					Thread.currentThread().setContextClassLoader(tcl);
				}
			}

			@Override
			public AttributedString toString(Message element, boolean selected) {
				try {
					String title = element.getSubject();
					AttributedString result = new AttributedString(title);
					if (selected) {
						result.setBackgroundColor(Color.BLUE);
						result.setForegroundColor(Color.BLACK);
					} else {
						result.setBackgroundColor(Color.NORMAL);
						result.setForegroundColor(Color.BLUE);
					}
					return result;
				} catch (MessagingException e) {
					return new AttributedString("");
				}
			}

		});
		_window.setRootView(listView);
		_window.pushResponderChain(listView.createResponder());
		_window.setNeedsFullRedraw();
		_window.refresh();
	}

}
