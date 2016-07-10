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
			LOG.trace("Found OSGi behavior immediately.");
			return CompletableFuture.completedFuture(new OSGiBehaviorCaller<T>(bc, ref));
		}

		LOG.trace("Couldn't find OSGi service.");

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

		LOG.trace("Found that some bundles are pending. Installing listener.");

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
									LOG.trace("Pending bundles started, found service.");
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
