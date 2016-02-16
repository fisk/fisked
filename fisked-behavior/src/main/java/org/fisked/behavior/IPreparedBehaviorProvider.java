package org.fisked.behavior;

public interface IPreparedBehaviorProvider {
	<T> IBehaviorConnection<T> getBehaviorConnection(Class<T> targetClass);
}
