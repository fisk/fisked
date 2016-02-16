package org.fisked.behavior.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;

public class NormalBehaviorProvider implements IBehaviorProvider {
	private IBehaviorProvider _parent;
	private final Map<Class<?>, Object> _map = new ConcurrentHashMap<>();

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
	public <C, T> IBehaviorConnection<T> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		Object mapResult = _map.get(targetClass);
		if (mapResult != null) {
			T service = targetClass.cast(mapResult);
			return new NormalBehaviorCaller<T>(service);
		}
		return _parent.getBehaviorConnection(callerClass, targetClass);
	}

	public <T> void addBehavior(Class<T> type, T behavior) {
		_map.put(type, behavior);
	}

}
