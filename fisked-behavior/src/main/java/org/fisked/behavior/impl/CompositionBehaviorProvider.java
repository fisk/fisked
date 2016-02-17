package org.fisked.behavior.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositionBehaviorProvider implements IBehaviorProvider {
	private final Map<String, IBehaviorProvider> _providerMap = new HashMap<>();
	private final List<IBehaviorProvider> _providerList = new ArrayList<>();
	private final static Logger LOG = LoggerFactory.getLogger(CompositionBehaviorProvider.class);

	@Override
	public <C, T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		if (_providerList.size() == 0)
			return null;
		LOG.debug("Calling up.");
		IBehaviorProvider provider = _providerList.get(_providerList.size() - 1);
		Future<IBehaviorConnection<T>> result = provider.getBehaviorConnection(callerClass, targetClass);
		return result;
	}

	public void addProvider(String name, IBehaviorProvider provider) {
		_providerList.add(provider);
		_providerMap.put(name, provider);
	}

}
