package org.fisked;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.launcher.service.ILauncherService;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = IFiskedCoreService.COMPONENT_NAME)
@Provides(specifications = { IFiskedCoreService.class })
public class FiskedCoreService implements IFiskedCoreService {
	@Requires
	private ILauncherService _launcherService;

	@Validate
	public void start() {
		runMain();
	}

	@Invalidate
	public void stop() {
	}

	@Override
	public void runMain() {
		String[] args = _launcherService.getMainArgs();
		Application application = Application.getApplication();
		application.start(args);
	}
}
