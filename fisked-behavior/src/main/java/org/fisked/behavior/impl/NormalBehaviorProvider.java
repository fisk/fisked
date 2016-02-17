package org.fisked.behavior.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalBehaviorProvider implements IBehaviorProvider {
	private IBehaviorProvider _parent;
	private final Map<Class<?>, Object> _map = new ConcurrentHashMap<>();
	private final static Logger LOG = LoggerFactory.getLogger(NormalBehaviorProvider.class);

	public class NormalBehaviorCaller<T> implements IBehaviorConnection<T> {
		private final T _behavior;

		public NormalBehaviorCaller(T behavior) {
			_behavior = behavior;
		}

		@Override
		public void close() throws Exception {

		}

		@Override
		public T getBehavior() {
			return _behavior;
		}

	}

	public NormalBehaviorProvider() {

	}

	public NormalBehaviorProvider(IBehaviorProvider parent) {
		_parent = parent;
	}

	@Override
	public <C, T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		Object mapResult = _map.get(targetClass);
		if (mapResult != null) {
			T service = targetClass.cast(mapResult);
			LOG.debug("Found normal behavior.");
			return CompletableFuture.completedFuture(new NormalBehaviorCaller<T>(service));
		}
		if (_parent != null) {
			LOG.debug("Calling up.");
			return _parent.getBehaviorConnection(callerClass, targetClass);
		} else {
			LOG.debug("No behavior found.");
			return null;
		}
	}

	public <T> void addBehavior(Class<T> type, T behavior) {
		_map.put(type, behavior);
	}

}
