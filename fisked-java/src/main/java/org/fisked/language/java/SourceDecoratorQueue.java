package org.fisked.language.java;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.fisked.buffer.BufferTextState;
import org.fisked.text.IBufferDecorator;
import org.fisked.util.concurrency.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceDecoratorQueue extends Thread implements IBufferDecorator {
	private final BlockingQueue<DecorationJob> _queue = new ArrayBlockingQueue<>(1024);
	private final IBufferDecorator _decorator;

	public SourceDecoratorQueue(IBufferDecorator decorator) {
		_decorator = decorator;
		start();
	}

	private class DecorationJob {
		final BufferTextState _state;
		final IBufferDecoratorCallback _callback;

		DecorationJob(BufferTextState state, IBufferDecoratorCallback callback) {
			_state = state;
			_callback = callback;
		}
	}

	final static Logger LOG = LoggerFactory.getLogger(SourceDecoratorQueue.class);

	private volatile boolean _terminate = false;

	public void terminate() {
		_terminate = true;
		interrupt();
	}

	@Override
	public void decorate(BufferTextState state, IBufferDecoratorCallback callback) {
		LOG.debug("Enqueued decoration");
		_queue.add(new DecorationJob(state, callback));
	}

	@Override
	public void run() {
		while (true) {
			if (_terminate)
				break;

			try {
				DecorationJob job = _queue.take();
				LOG.debug("Got a job");
				DecorationJob newerJob = job;
				while (newerJob != null) {
					job = newerJob;
					newerJob = _queue.poll();
					LOG.debug("Found a newer job");
				}

				DecorationJob decorationJob = job;
				LOG.debug("Decided on a job");

				_decorator.decorate(decorationJob._state, attrString -> {
					LOG.debug("Decorator finished");
					Dispatcher.getInstance().runMain(() -> {
						LOG.debug("Callback on main");
						decorationJob._callback.call(attrString);
					});
				});
			} catch (InterruptedException e) {
			}
		}
	}

}
