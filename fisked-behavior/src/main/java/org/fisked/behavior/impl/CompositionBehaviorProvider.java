package org.fisked.behavior.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;

public class CompositionBehaviorProvider implements IBehaviorProvider {
	private final Map<String, IBehaviorProvider> _providerMap = new HashMap<>();
	private final List<IBehaviorProvider> _providerList = new ArrayList<>();

	@Override
	public <C, T> IBehaviorConnection<T> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		for (IBehaviorProvider provider : _providerList) {
			IBehaviorConnection<T> result = provider.getBehaviorConnection(callerClass, targetClass);
			if (result != null)
				return result;
		}
		return null;
	}

	public void addProvider(String name, IBehaviorProvider provider) {
		_providerList.add(provider);
		_providerMap.put(name, provider);
	}

}
