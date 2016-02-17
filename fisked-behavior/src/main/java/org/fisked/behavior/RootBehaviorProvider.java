package org.fisked.behavior;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fisked.behavior.impl.CompositionBehaviorProvider;
import org.fisked.behavior.impl.NormalBehaviorProvider;
import org.fisked.behavior.impl.OSGiBehaviorProvider;
import org.fisked.behavior.impl.RMI_IIOPBehaviorProvider;
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
		RMI_IIOPBehaviorProvider rmi_iiop = new RMI_IIOPBehaviorProvider(osgi);
		NormalBehaviorProvider normal = new NormalBehaviorProvider(rmi_iiop);
		CompositionBehaviorProvider intent = new CompositionBehaviorProvider();
		intent.addProvider("osgi", osgi);
		intent.addProvider("rmi_iiop", rmi_iiop);
		intent.addProvider("normal", normal);

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
