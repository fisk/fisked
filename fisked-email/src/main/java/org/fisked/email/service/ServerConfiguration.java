package org.fisked.email.service;

import java.io.Serializable;

public abstract class ServerConfiguration implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -7692291867735382999L;
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
