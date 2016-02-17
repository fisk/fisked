package org.fisked.email.service;

import java.io.Serializable;

public class EmailProfile implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6507442732294783090L;
	private String _id;
	private String _name;
	private String _email;
	private ServerConfiguration _receiveServer;
	private ServerConfiguration _sendServer;

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getEmail() {
		return _email;
	}

	public void setEmail(String email) {
		_email = email;
	}

	public ServerConfiguration getReceiveServer() {
		return _receiveServer;
	}

	public void setReceiveServer(ServerConfiguration receiveServer) {
		_receiveServer = receiveServer;
	}

	public ServerConfiguration getSendServer() {
		return _sendServer;
	}

	public void setSendServer(ServerConfiguration sendServer) {
		_sendServer = sendServer;
	}

}
