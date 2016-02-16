package org.fisked.email.service;

import javax.mail.Folder;

public interface IEmailFetchService {
	public static String DYNAMICALLY_FIND_PROTOCOL_FILTER = "(protocol=default)";

	public interface IEmailFetchFolderCallback {
		void success(Folder folder);

		void error(EmailProfile profile, String name, Exception e);
	}

	public interface IEmailFetchFoldersCallback {
		void success(Folder[] folders);

		void error(EmailProfile profile, Exception e);
	}

	void getFolder(EmailProfile profile, String name, IEmailFetchFolderCallback callback);

	void getFolders(EmailProfile profile, IEmailFetchFoldersCallback callback);
}
