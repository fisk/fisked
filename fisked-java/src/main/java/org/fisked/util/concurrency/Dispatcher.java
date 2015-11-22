package org.fisked.util.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.fisked.Application;
import org.fisked.util.Singleton;

public class Dispatcher {
	private final ExecutorService _pool = Executors.newCachedThreadPool();
	private Thread _mainThread;

	public static Dispatcher getInstance() {
		return Singleton.getInstance(Dispatcher.class);
	}

	public void setMainThread(Thread thread) {
		_mainThread = thread;
	}

	public <T> Future<T> futureConc(Callable<T> callable) {
		return _pool.submit(callable);
	}

	public void runConc(Runnable runnable) {
		_pool.execute(runnable);
	}

	public void runMain(Runnable runnable) {
		if (Thread.currentThread() == _mainThread) {
			runnable.run();
		} else {
			Application.getApplication().getEventLoop().run(runnable);
		}
	}
}
