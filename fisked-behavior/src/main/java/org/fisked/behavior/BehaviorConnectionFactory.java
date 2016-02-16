package org.fisked.behavior;

public class BehaviorConnectionFactory<C> implements IPreparedBehaviorProvider {
	private final Class<C> _callerClass;
	private final IBehaviorProvider _provider;

	public BehaviorConnectionFactory(Class<C> callerClass) {
		_provider = RootBehaviorProvider.getInstance().getBehaviorProvider(callerClass);
		_callerClass = callerClass;
	}

	public BehaviorConnectionFactory(IBehaviorProvider provider, Class<C> callerClass) {
		_provider = provider;
		_callerClass = callerClass;
	}

	@Override
	public <T> IBehaviorConnection<T> getBehaviorConnection(Class<T> targetClass) {
		return _provider.getBehaviorConnection(_callerClass, targetClass);
	}
}
