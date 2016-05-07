/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.email.imap;

import org.fisked.email.service.ServerConfiguration;

public class IMAPServerConfiguration extends ServerConfiguration {
	/**
	 *
	 */
	private static final long serialVersionUID = -1428145605686343571L;
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
