package org.fisked.email.service;

public interface IEmailProfileVendor {
	EmailProfile getEmailProfileByEmail(String email);

	EmailProfile[] getEmailProfiles(String email);
}
