package org.fisked.email.service;

public interface IEmailSendService {
	public static String DYNAMICALLY_FIND_PROTOCOL_FILTER = "(protocol=default)";

	public interface IEmailSendCallback {
		void success(Email email, EmailProfile profile);

		void error(Email email, EmailProfile profile, Exception e);
	}

	void sendEmail(Email email, EmailProfile profile, IEmailSendCallback callback);
}
