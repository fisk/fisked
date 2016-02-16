package org.fisked.email.imap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.fisked.email.service.IEmailServerParser;
import org.fisked.email.service.ServerConfiguration;
import org.json.JSONObject;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = IEmailServerParser.class)
public class IMAPEmailServerParser implements IEmailServerParser {
	@ServiceProperty(name = "protocol", value = "imap")
	private String _protocol;

	@Override
	public ServerConfiguration getConfiguration(JSONObject json) {
		IMAPServerConfiguration server = new IMAPServerConfiguration();

		if (json.has("host"))
			server.setHost(json.getString("host"));
		if (json.has("port"))
			server.setPort(Long.toString(json.getLong("port")));
		if (json.has("auth"))
			server.setAuth(json.getBoolean("auth"));
		if (json.has("ssl"))
			server.setSsl(json.getBoolean("ssl"));
		if (json.has("username"))
			server.setUsername(json.getString("username"));
		if (json.has("password"))
			server.setPassword(json.getString("password"));

		return server;
	}

}
