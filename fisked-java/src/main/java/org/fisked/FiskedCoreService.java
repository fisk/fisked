package org.fisked;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.launcher.service.ILauncherService;
import org.osgi.framework.BundleContext;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = IFiskedCoreService.COMPONENT_NAME)
@Provides(specifications = { IFiskedCoreService.class })
public class FiskedCoreService implements IFiskedCoreService {
	@Requires
	private ILauncherService _launcherService;

	@Context
	private BundleContext _context;

	@Validate
	public void start() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				setName("Fisked Main Thread");
				runMain();
			}
		};
		thread.start();
	}

	@Invalidate
	public void stop() {
	}

	@Override
	public void runMain() {
		String[] args = _launcherService.getMainArgs();
		Application application = new Application(_launcherService, _context);
		application.start(args);
	}
}
