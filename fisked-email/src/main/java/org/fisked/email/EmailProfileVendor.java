package org.fisked.email;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailProfileVendor;
import org.fisked.email.service.IEmailServerParser;
import org.fisked.email.service.ServerConfiguration;
import org.fisked.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = IEmailProfileVendor.class)
public class EmailProfileVendor implements IEmailProfileVendor {
	private final static Logger LOG = LoggerFactory.getLogger(EmailProfileVendor.class);

	private ServiceReference<IEmailServerParser> getService(String protocol) {
		BundleContext context = FrameworkUtil.getBundle(IEmailServerParser.class).getBundleContext();
		try {
			Collection<ServiceReference<IEmailServerParser>> refs = context
					.getServiceReferences(IEmailServerParser.class, "(protocol=" + protocol + ")");

			if (refs.size() != 1) {
				throw new UnsupportedProtocolException("Protocol providers for " + protocol + ": " + refs.size());
			}

			return refs.iterator().next();
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private class Config {
		private final Map<String, ServerConfiguration> _idToServer = new ConcurrentHashMap<>();
		private final Map<String, EmailProfile> _emailToProfile = new ConcurrentHashMap<>();
		private final Map<String, EmailProfile> _idToProfile = new ConcurrentHashMap<>();
		private final List<EmailProfile> _profileList = new ArrayList<>();

		private void parseServer(JSONObject serverMap) {
			String protocol = serverMap.getString("protocol");
			String id = serverMap.getString("id");

			if (id == null || _idToServer.get(id) != null) {
				throw new RuntimeException("Invalid server ID: " + id);
			}

			if (protocol == null) {
				throw new NullPointerException("Can't have null protocol for server.");
			}

			BundleContext context = FrameworkUtil.getBundle(EmailFetchService.class).getBundleContext();
			ServiceReference<IEmailServerParser> ref = getService(protocol);
			LOG.debug("Email parser found parsing service for protocol.");
			IEmailServerParser service = context.getService(ref);
			ServerConfiguration server = service.getConfiguration(serverMap);
			context.ungetService(ref);

			server.setId(id);

			_idToServer.put(id, server);
		}

		private void parseProfile(JSONObject profileMap) {
			EmailProfile profile = new EmailProfile();

			String id = profileMap.getString("id");
			String email = profileMap.getString("email");
			String name = profileMap.getString("name");
			String receive = profileMap.getString("receive");
			String send = profileMap.getString("send");

			if (id == null || _idToProfile.get(id) != null) {
				throw new RuntimeException("Invalid profile ID: " + id);
			}

			if (email == null || _emailToProfile.get(email) != null) {
				throw new RuntimeException("Invalid profile email: " + email);
			}

			ServerConfiguration receiveServer = _idToServer.get(receive);
			ServerConfiguration sendServer = _idToServer.get(send);

			if (receiveServer == null || !receiveServer.isReceiver()) {
				throw new RuntimeException("Invalid receive server: " + receive);
			}

			if (sendServer == null || sendServer.isReceiver()) {
				throw new RuntimeException("Invalid send server: " + send);
			}

			profile.setName(name);
			profile.setReceiveServer(receiveServer);
			profile.setSendServer(sendServer);

			_idToProfile.put(id, profile);
			_emailToProfile.put(email, profile);
			_profileList.add(profile);
		}

		private void clearConfig() {
			_idToServer.clear();
			_emailToProfile.clear();
			_idToProfile.clear();
			_profileList.clear();
		}

		private void parseConfig() {
			File file = FileUtil.getFile("~/.fisked/email.json");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					LOG.error("Could not create email config file: ", e);
				}
			}
			LOG.debug("Using email config file: " + file.getAbsolutePath());
			try {
				String fileJSON = FileUtils.readFileToString(file);
				LOG.debug("Email json: " + fileJSON);
				JSONObject json = new JSONObject(fileJSON);
				JSONArray servers = json.getJSONArray("servers");
				for (int i = 0; i < servers.length(); i++) {
					JSONObject server = servers.getJSONObject(i);
					parseServer(server);
				}
				JSONArray profiles = json.getJSONArray("profiles");
				for (int i = 0; i < profiles.length(); i++) {
					JSONObject profile = profiles.getJSONObject(i);
					parseProfile(profile);
				}
			} catch (Exception e) {
				LOG.error("Could not read email config file: " + e);
				clearConfig();
			}
		}

	}

	public EmailProfileVendor() {
	}

	@Override
	public EmailProfile getEmailProfileByEmail(String email) {
		Config config = new Config();
		config.parseConfig();
		return config._emailToProfile.get(email);
	}

	@Override
	public EmailProfile[] getEmailProfiles(String email) {
		Config config = new Config();
		config.parseConfig();
		return config._profileList.toArray(new EmailProfile[] {});
	}

}
