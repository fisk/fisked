package org.fisked.behavior.impl;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class OSGiBehaviorProvider implements IBehaviorProvider {

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
	public <C, T> IBehaviorConnection<T> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		BundleContext bc = FrameworkUtil.getBundle(callerClass).getBundleContext();
		ServiceReference<T> ref = bc.getServiceReference(targetClass);
		if (ref == null)
			return null;
		return new OSGiBehaviorCaller<T>(bc, ref);
	}

}
