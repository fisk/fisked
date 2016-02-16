package org.fisked.email.imap;

import org.fisked.email.service.ServerConfiguration;

public class IMAPServerConfiguration extends ServerConfiguration {
	private String _host;
	private String _port;
	private boolean _ssl = false;
	private boolean _auth = false;
	private String _username;
	private String _password;

	public IMAPServerConfiguration() {
		super("imap");
	}

	public String getHost() {
		return _host;
	}

	public void setHost(String host) {
		_host = host;
	}

	public String getPort() {
		if (_port == null) {
			if (_ssl) {
				return "993";
			} else {
				return "143";
			}
		} else {
			return _port;
		}
	}

	public void setPort(String port) {
		_port = port;
	}

	public boolean hasSsl() {
		return _ssl;
	}

	public void setSsl(boolean ssl) {
		_ssl = ssl;
	}

	public boolean hasAuth() {
		return _auth;
	}

	public void setAuth(boolean auth) {
		_auth = auth;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}

	@Override
	public boolean isReceiver() {
		return true;
	}

}
