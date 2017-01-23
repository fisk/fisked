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
package org.fisked.email.imap;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.fisked.email.UnsupportedProtocolException;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = IEmailFetchService.class)
public class IMAPEmailFetchService implements IEmailFetchService {
	private final static Logger LOG = LoggerFactory.getLogger(IMAPEmailFetchService.class);
	@ServiceProperty(name = "protocol", value = "imap")
	private String _protocol;

	private interface IGeneralEmailErrorCallback {
		void error(Exception e);
	}

	private Store connect(EmailProfile profile, IGeneralEmailErrorCallback callback) {
		IMAPServerConfiguration server = (IMAPServerConfiguration) profile.getReceiveServer();
		if (!server.hasSsl()) {
			callback.error(new UnsupportedProtocolException("Can't connect without SSL yet."));
			return null;
		}
		if (!server.hasAuth() || server.getUsername() == null || server.getPassword() == null) {
			callback.error(new UnsupportedProtocolException("Insufficient authentication details."));
			return null;
		}
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");

		Session session = Session.getDefaultInstance(props, null);

		try {
			Store store = session.getStore("imaps");
			store.connect(server.getHost(), server.getUsername(), server.getPassword());
			return store;
		} catch (Exception e) {
			callback.error(e);
		}

		return null;
	}

	@Override
	public void getFolder(EmailProfile profile, String name, IEmailFetchFolderCallback callback) {
		Store store = connect(profile, (e) -> {
			callback.error(profile, name, e);
		});
		if (store == null)
			return;
		Folder folder = null;
		String subject = null;
		try {

			folder = store.getFolder(name);

			if (!folder.isOpen())
				folder.open(Folder.READ_WRITE);
			int count = folder.getMessageCount();
			Message[] messages = folder.getMessages(count - 10 + 1, count);
			LOG.debug("No of Messages : " + folder.getMessageCount());
			LOG.debug("No of Unread Messages : " + folder.getUnreadMessageCount());
			LOG.debug("" + messages.length);
			for (int i = 0; i < messages.length; i++) {

				LOG.debug("*****************************************************************************");
				LOG.debug("MESSAGE " + (i + 1) + ":");
				Message msg = messages[i];

				subject = msg.getSubject();

				LOG.debug("Subject: " + subject);
				LOG.debug("From: " + msg.getFrom()[0]);
				LOG.debug("To: " + msg.getAllRecipients()[0]);
				LOG.debug("Date: " + msg.getReceivedDate());
				LOG.debug("Size: " + msg.getSize());
				LOG.debug("Flags: " + msg.getFlags());
				LOG.debug("Body: \n" + msg.getContent());
				LOG.debug("Content type: " + msg.getContentType());

			}
		} catch (Exception e) {
			callback.error(profile, name, e);
		} finally {
			if (folder != null && folder.isOpen()) {
				try {
					folder.close(true);
				} catch (MessagingException e) {
					callback.error(profile, name, e);
				}
			}
			if (store != null) {
				try {
					store.close();
				} catch (MessagingException e) {
					callback.error(profile, name, e);
				}
			}
		}
	}

	@Override
	public void getFolders(EmailProfile profile, IEmailFetchFoldersCallback callback) {
		Store store = connect(profile, (e) -> {
			callback.error(profile, e);
		});
		if (store == null)
			return;
		try {
			Folder[] folders = store.getDefaultFolder().list("*");
			callback.success(folders);
		} catch (MessagingException e) {
			callback.error(profile, e);
		}
	}

}
