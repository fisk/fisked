package org.fisked.behavior;

import java.util.concurrent.Future;

public interface IPreparedBehaviorProvider {
	<T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<T> targetClass);
}
