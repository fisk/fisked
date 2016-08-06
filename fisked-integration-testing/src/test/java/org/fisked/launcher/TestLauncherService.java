package org.fisked.launcher;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.launcher.service.ILauncherService;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = { ILauncherService.class })
public class TestLauncherService implements ILauncherService {

	@Override
	public String[] getMainArgs() {
		return null;
	}

	@Override
	public void stop(int code) {
		System.exit(code);
	}

}
