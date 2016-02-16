package org.fisked.email;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailFetchService;
import org.fisked.email.service.ServerConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = IEmailFetchService.class)
public class EmailFetchService implements IEmailFetchService {
	@ServiceProperty(name = "protocol", value = "default")
	private String _protocol;
	private final static Logger LOG = LoggerFactory.getLogger(EmailFetchService.class);

	private ServiceReference<IEmailFetchService> getService(EmailProfile profile) {
		String protocol = profile.getReceiveServer().getProtocol();

		BundleContext context = FrameworkUtil.getBundle(EmailFetchService.class).getBundleContext();
		try {
			Collection<ServiceReference<IEmailFetchService>> refs = context
					.getServiceReferences(IEmailFetchService.class, "(protocol=" + protocol + ")");

			if (refs.size() != 1) {
				throw new UnsupportedProtocolException("Protocol providers for " + protocol + ": " + refs.size());
			}

			return refs.iterator().next();
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void getFolder(EmailProfile profile, String name, IEmailFetchFolderCallback callback) {
		if (profile == null) {
			callback.error(profile, name, new NullPointerException("No email profile specified."));
			return;
		}

		ServerConfiguration server = profile.getReceiveServer();
		if (server == null) {
			callback.error(profile, name, new UnsupportedProtocolException("Unknown email protocol for fetching."));
			return;
		}

		BundleContext context = FrameworkUtil.getBundle(EmailFetchService.class).getBundleContext();
		ServiceReference<IEmailFetchService> ref = getService(profile);
		LOG.debug("Email fetcher found service for protocol.");
		IEmailFetchService service = context.getService(ref);
		service.getFolder(profile, name, callback);
		context.ungetService(ref);
	}

	@Override
	public void getFolders(EmailProfile profile, IEmailFetchFoldersCallback callback) {
		if (profile == null) {
			callback.error(profile, new NullPointerException("No email profile specified."));
			return;
		}

		ServerConfiguration server = profile.getReceiveServer();
		if (server == null) {
			callback.error(profile, new UnsupportedProtocolException("Unknown email protocol for fetching."));
			return;
		}

		BundleContext context = FrameworkUtil.getBundle(EmailFetchService.class).getBundleContext();
		ServiceReference<IEmailFetchService> ref = getService(profile);
		IEmailFetchService service = context.getService(ref);
		LOG.debug("Email fetcher found service for protocol.");
		service.getFolders(profile, callback);
		context.ungetService(ref);
	}

}
