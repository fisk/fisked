package org.fisked.behavior;

import java.util.concurrent.Future;

public interface IBehaviorProvider {
	<C, T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass);
}
