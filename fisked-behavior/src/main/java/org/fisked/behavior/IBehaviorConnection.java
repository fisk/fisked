package org.fisked.behavior;

public interface IBehaviorConnection<T> extends AutoCloseable {
	T getBehavior();
}
