package org.fisked.email.ui;

import javax.mail.Folder;

import org.fisked.email.service.EmailProfile;

public class Account {
	private final EmailProfile _profile;
	private final Folder[] _folders;

	public Account(EmailProfile profile, Folder[] folders) {
		_profile = profile;
		_folders = folders;
	}

	public EmailProfile getProfile() {
		return _profile;
	}

	public Folder[] getFolders() {
		return _folders;
	}
}
