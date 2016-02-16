package org.fisked.email.service;

import org.json.JSONObject;

public interface IEmailServerParser {
	ServerConfiguration getConfiguration(JSONObject json);
}
