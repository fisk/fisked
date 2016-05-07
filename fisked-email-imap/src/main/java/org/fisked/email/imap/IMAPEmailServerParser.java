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
