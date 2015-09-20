package org.fisked.util.concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.fisked.util.Singleton;

public class Dispatcher {
	ExecutorService _pool = Executors.newCachedThreadPool();
	Queue<Runnable> _mainQueue = new LinkedList<Runnable>();
	
	public static Dispatcher getInstance() {
		return Singleton.getInstance(Dispatcher.class);
	}
	
	public <T> Future<T> futureConc(Callable<T> callable) {
		return _pool.submit(callable);
	}
	
	public void runConc(Runnable runnable) {
		_pool.execute(runnable);
	}
	
	public void runMain(Runnable runnable) {
		_mainQueue.offer(runnable);
	}
}
