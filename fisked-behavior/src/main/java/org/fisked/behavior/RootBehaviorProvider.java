package org.fisked.behavior;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fisked.behavior.impl.CompositionBehaviorProvider;
import org.fisked.behavior.impl.NormalBehaviorProvider;
import org.fisked.behavior.impl.OSGiBehaviorProvider;
import org.fisked.util.Singleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class RootBehaviorProvider {
	private final Map<BundleContext, CompositionBehaviorProvider> _map = new ConcurrentHashMap<>();

	public <C> CompositionBehaviorProvider getBehaviorProvider(Class<C> callerClass) {
		Bundle bundle = FrameworkUtil.getBundle(callerClass);
		BundleContext context = bundle.getBundleContext();

		CompositionBehaviorProvider provider = _map.get(context);
		if (provider != null) {
			return provider;
		}

		OSGiBehaviorProvider osgi = new OSGiBehaviorProvider();
		NormalBehaviorProvider normal = new NormalBehaviorProvider();
		CompositionBehaviorProvider intent = new CompositionBehaviorProvider();
		intent.addProvider("normal", normal);
		intent.addProvider("osgi", osgi);

		provider = _map.put(context, intent);
		if (provider != null) {
			return provider;
		}
		return intent;
	}

	public static RootBehaviorProvider getInstance() {
		return Singleton.getInstance(RootBehaviorProvider.class);
	}

}
