package org.fisked.behavior.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSGiBehaviorProvider implements IBehaviorProvider {
	private final static Logger LOG = LoggerFactory.getLogger(OSGiBehaviorProvider.class);

	private class OSGiBehaviorCaller<T> implements IBehaviorConnection<T> {
		private final ServiceReference<T> _ref;
		private final BundleContext _bc;

		OSGiBehaviorCaller(BundleContext bc, ServiceReference<T> ref) {
			_bc = bc;
			_ref = ref;
		}

		@Override
		public void close() throws Exception {
			_bc.ungetService(_ref);
		}

		@Override
		public T getBehavior() {
			T service = _bc.getService(_ref);
			return service;
		}

	}

	@Override
	public <C, T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		final BundleContext bc = FrameworkUtil.getBundle(callerClass).getBundleContext();
		ServiceReference<T> ref = bc.getServiceReference(targetClass);
		if (ref != null) {
			LOG.debug("Found OSGi behavior immediately.");
			return CompletableFuture.completedFuture(new OSGiBehaviorCaller<T>(bc, ref));
		}

		LOG.debug("Couldn't find OSGi service.");

		Bundle[] bundles = bc.getBundles();
		List<Bundle> problemBundles = new ArrayList<>();

		for (Bundle bundle : bundles) {
			if (bundle.getState() != Bundle.ACTIVE) {
				problemBundles.add(bundle);
			}
		}

		if (problemBundles.size() == 0) {
			throw new RuntimeException("Could not find OSGI behavior: " + targetClass.getName());
		}

		AtomicInteger needsWait = new AtomicInteger(problemBundles.size());

		LOG.debug("Found that some bundles are pending. Installing listener.");

		CompletableFuture<IBehaviorConnection<T>> future = new CompletableFuture<IBehaviorConnection<T>>();
		try {
			final ServiceListener listener = new ServiceListener() {
				@Override
				public void serviceChanged(ServiceEvent event) {
					if (event.getType() == ServiceEvent.REGISTERED) {
						@SuppressWarnings("unchecked")
						ServiceReference<T> reference = (ServiceReference<T>) event.getServiceReference();
						OSGiBehaviorCaller<T> connection = new OSGiBehaviorCaller<T>(bc, reference);
						bc.removeServiceListener(this);
						future.complete(connection);
					}
				}
			};
			bc.addServiceListener(listener, "(objectclass=" + targetClass.getName() + ")");

			for (Bundle bundle : problemBundles) {
				final BundleListener bundleListener = new BundleListener() {
					@Override
					public void bundleChanged(BundleEvent event) {
						if (needsWait.get() == 0) {
							bc.removeBundleListener(this);
						}
						if (event.getBundle() != bundle)
							return;
						if (event.getType() == BundleEvent.STARTED) {
							int count = needsWait.decrementAndGet();
							if (count == 0) {
								bc.removeServiceListener(listener);
								bc.removeBundleListener(this);
								if (!future.isDone()) {
									LOG.error("Pending bundles started, still no service. Ouch.");
									future.cancel(true);
								} else {
									LOG.debug("Pending bundles started, found service.");
								}
							}
						}
					}
				};
				bc.addBundleListener(bundleListener);
			}
		} catch (InvalidSyntaxException e) {
			LOG.error("Invalid syntax: " + e);
		}
		return future;
	}

}
