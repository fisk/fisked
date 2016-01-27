package org.fisked.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.main.AutoProcessor;
import org.fisked.launcher.service.ILauncherService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class Launcher {
	private final String[] _args;

	public class LauncherActivator implements BundleActivator {
		private BundleContext _context;
		private ServiceRegistration<ILauncherService> _registration;

		@Override
		public void start(BundleContext context) {
			_context = context;
			ILauncherService service = () -> _args;
			_registration = context.registerService(ILauncherService.class, service, null);
		}

		@Override
		public void stop(BundleContext context) {
			_registration.unregister();
			_context = null;
		}

		public Bundle[] getBundles() {
			if (_context != null) {
				return _context.getBundles();
			}
			return null;
		}
	}

	private LauncherActivator _activator;
	private Felix _felix;

	public Launcher(String[] args) {
		_args = args;
	}

	private String state(int state) {
		switch (state) {
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.ACTIVE:
			return "ACTIVE";
		default:
			return "UNKNOWN";
		}
	}

	private void printBundles() {
		System.out.println("Bundle snapshot: ");
		for (Bundle bundle : _activator.getBundles()) {
			System.out.println("Bundle " + bundle.getSymbolicName() + ": " + state(bundle.getState()));
		}
	}

	public void start() {
		Map<String, Object> config = new HashMap<>();
		config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.fisked.launcher.service; version=1.0.0");

		List<LauncherActivator> list = new ArrayList<>();
		_activator = new LauncherActivator();
		list.add(_activator);
		config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

		config.put("felix.auto.deploy.action", "install,start");
		config.put("felix.auto.deploy.dir", "bundle");
		config.put("org.osgi.framework.storage.clean", "onFirstInit");
		config.put(Constants.FRAMEWORK_STORAGE, "bundle-cache");

		try {
			_felix = new Felix(config);
		} catch (Exception e) {
			System.err.println("Could not create framework: " + e);
			e.printStackTrace();
		}
		try {
			_felix.init();
			AutoProcessor.process(config, _felix.getBundleContext());
			_felix.start();
		} catch (BundleException e) {
			System.err.println("Could not start framework: " + e);
			e.printStackTrace();
		}
	}

	public Bundle[] getInstalledBundles() {
		return _activator.getBundles();
	}

	public void stop() {
		try {
			_felix.stop();
			_felix.waitForStop(0);
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		Launcher launcher = new Launcher(args);
		launcher.start();
	}
}