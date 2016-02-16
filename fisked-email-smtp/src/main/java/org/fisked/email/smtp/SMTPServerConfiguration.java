package org.fisked.email.smtp;

import org.fisked.email.service.ServerConfiguration;

public class SMTPServerConfiguration extends ServerConfiguration {
	private String _host;
	private String _port = "587";
	private boolean _tls = false;
	private boolean _auth = false;
	private String _username;
	private String _password;

	public SMTPServerConfiguration() {
		super("smtp");
	}

	public String getHost() {
		return _host;
	}

	public void setHost(String _host) {
		this._host = _host;
	}

	public String getPort() {
		return _port;
	}

	public void setPort(String _port) {
		this._port = _port;
	}

	public boolean hasTls() {
		return _tls;
	}

	public void setTls(boolean _tls) {
		this._tls = _tls;
	}

	public boolean hasAuth() {
		return _auth;
	}

	public void setAuth(boolean _auth) {
		this._auth = _auth;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String _username) {
		this._username = _username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String _password) {
		this._password = _password;
	}

	@Override
	public boolean isReceiver() {
		return false;
	}
}
