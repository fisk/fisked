package org.fisked.behavior;

public interface IBehaviorProvider {
	<C, T> IBehaviorConnection<T> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass);
}
