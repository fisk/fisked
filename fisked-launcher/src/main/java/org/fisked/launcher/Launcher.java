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
			ILauncherService service = new ILauncherService() {

				@Override
				public String[] getMainArgs() {
					return _args;
				}

				@Override
				public void stop(int code) {
					Launcher.this.stop(code);
				}

				@Override
				public void printBundles() {
					Launcher.this.printBundles();
				}

			};
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

	public void printBundles() {
		System.out.println("Bundle snapshot: ");
		for (Bundle bundle : _activator.getBundles()) {
			System.out.println("Bundle " + bundle.getSymbolicName() + ": " + state(bundle.getState()));
		}
	}

	private String join(List<String> strs, String delim) {
		StringBuilder str = new StringBuilder();
		int i = 0;
		for (String substr : strs) {
			str.append(substr);
			if (++i != strs.size()) {
				str.append(delim);
			}
		}
		return str.toString();
	}

	public void start() {
		Map<String, Object> config = new HashMap<>();

		List<String> deps = new ArrayList<>();
		deps.add("org.fisked.launcher.service; version=1.0.0");
		deps.add("sun.misc");
		deps.add("org.python");
		deps.add("org.python.util");

		config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, join(deps, ", "));

		List<LauncherActivator> list = new ArrayList<>();
		_activator = new LauncherActivator();
		list.add(_activator);
		config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

		config.put("felix.auto.deploy.action", "install,start");
		config.put("felix.auto.deploy.dir", System.getProperty("user.home") + "/.fisked/bundle");
		config.put("org.osgi.framework.storage.clean", "onFirstInit");
		config.put(Constants.FRAMEWORK_STORAGE, System.getProperty("user.home") + "/.fisked/bundle-cache");
		// config.put(AutoProcessor.AUTO_DEPLOY_STARTLEVEL_PROPERTY, "1");

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

	private volatile boolean _stopRequested;
	private int _stopCode;

	public void stop(int code) {
		_stopCode = code;
		_stopRequested = true;
		synchronized (this) {
			notify();
		}
		System.exit(_stopCode);
	}

	private void finalExit() {
		try {
			_felix.stop();
			_felix.waitForStop(0);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		System.exit(_stopCode);
	}

	public static void main(String[] args) {
		Launcher launcher = new Launcher(args);
		launcher.start();
		launcher.waitForExitLoop();
	}

	private void waitForExitLoop() {
		synchronized (this) {
			while (!_stopRequested) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
			finalExit();
		}
	}
}