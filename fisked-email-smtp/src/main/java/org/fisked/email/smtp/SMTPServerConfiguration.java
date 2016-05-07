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
package org.fisked.email.smtp;

import org.fisked.email.service.ServerConfiguration;

public class SMTPServerConfiguration extends ServerConfiguration {
	/**
	 *
	 */
	private static final long serialVersionUID = 317886025695573474L;
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
