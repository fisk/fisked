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
