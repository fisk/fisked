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
package org.fisked.email;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.fisked.email.service.Email;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailSendService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = IEmailSendService.class)
public class EmailSendService implements IEmailSendService {
	@ServiceProperty(name = "protocol", value = "default")
	private String _protocol;
	private final static Logger LOG = LoggerFactory.getLogger(EmailSendService.class);

	@Override
	public void sendEmail(Email email, EmailProfile profile, IEmailSendCallback callback) {
		if (profile == null) {
			callback.error(email, profile, new NullPointerException("No email profile specified."));
			return;
		}

		String protocol = profile.getSendServer().getProtocol();

		BundleContext context = FrameworkUtil.getBundle(EmailSendService.class).getBundleContext();
		try {
			Collection<ServiceReference<IEmailSendService>> refs = context.getServiceReferences(IEmailSendService.class,
					"(protocol=" + protocol + ")");

			if (refs.size() != 1) {
				throw new UnsupportedProtocolException("Protocol providers for " + protocol + ": " + refs.size());
			}

			ServiceReference<IEmailSendService> ref = refs.iterator().next();
			IEmailSendService service = context.getService(ref);
			LOG.debug("Email sender found service for protocol.");
			service.sendEmail(email, profile, callback);
			context.ungetService(ref);
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
