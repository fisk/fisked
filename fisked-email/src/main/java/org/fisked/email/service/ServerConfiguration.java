package org.fisked.email.service;

public abstract class ServerConfiguration {
	private String _id;
	private String _protocol;

	public ServerConfiguration(String protocol) {
		_protocol = protocol;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getProtocol() {
		return _protocol;
	}

	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	public abstract boolean isReceiver();
}
