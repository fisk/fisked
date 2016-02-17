package org.fisked.behavior;

import java.util.concurrent.Future;

public class BehaviorConnectionFactory implements IPreparedBehaviorProvider {
	private final Class<?> _callerClass;
	private final IBehaviorProvider _provider;

	public BehaviorConnectionFactory(Class<?> callerClass) {
		_provider = RootBehaviorProvider.getInstance().getBehaviorProvider(callerClass);
		_callerClass = callerClass;
	}

	public BehaviorConnectionFactory(IBehaviorProvider provider, Class<?> callerClass) {
		_provider = provider;
		_callerClass = callerClass;
	}

	@Override
	public <T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<T> targetClass) {
		return _provider.getBehaviorConnection(_callerClass, targetClass);
	}
}
