package org.fisked.responder;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.fisked.buffer.drawing.Window;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.services.ServiceManager;
import org.fisked.util.concurrency.IRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLoop implements IRunner {
	private final static Logger LOG = LoggerFactory.getLogger(EventLoop.class);
	private final BlockingQueue<Runnable> _queue = new ArrayBlockingQueue<>(1024);
	private Window _primaryResponder;
	private IOThread _iothread;
	private volatile boolean _exitRequested;

	@Override
	public void run(Runnable runnable) {
		_queue.add(runnable);
	}

	private void sendChar(int nextChar) {
		run(() -> {
			LOG.info("Runloop got character: " + nextChar);
			Event nextEvent = new Event(nextChar);
			if (postponedEnd != null) {
				postponedEnd.setNext(nextEvent);
				postponedEnd = nextEvent;
			} else {
				postponedStart = postponedEnd = nextEvent;
			}
			RecognitionState state = _primaryResponder.recognizesInput(postponedStart);
			switch (state) {
			case Recognized:
				_primaryResponder.onRecognize();
			case NotRecognized:
				postponedStart = postponedEnd = null;
				break;

			case MaybeRecognized:
				break;
			}
		});
	}

	private void pollEvents() {
		LOG.debug("Polling events in event loop.");
		Runnable runnable;
		try {
			runnable = _queue.take();
			LOG.debug("Got event in event loop.");
			runnable.run();
		} catch (InterruptedException e) {
		}
		while ((runnable = _queue.poll()) != null) {
			runnable.run();
		}
	}

	private Event postponedStart = null;
	private Event postponedEnd = null;
	private final Object _sema = new Object();

	private int getNextChar() throws IOException {
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();

		return cs.getChar();
	}

	private class IOThread extends Thread {
		private boolean _requestingChar = true;

		public IOThread() {
			super("Fisked Async IO Thread");
		}

		@Override
		public void run() {
			LOG.debug("Starting async IO thread.");
			while (true) {
				try {
					synchronized (_sema) {
						while (true) {
							LOG.debug("Waiting for IO.");
							if (!_requestingChar) {
								_sema.wait();
							}
							LOG.debug("Wake up IO thread.");
							if (_exitRequested) {
								LOG.debug("Shutting down IO thread.");
								return;
							}
							if (_requestingChar)
								break;
						}
					}
					int character = getNextChar();
					LOG.info("Async IO thread got character: " + character);
					synchronized (_sema) {
						_requestingChar = false;
					}
					sendChar(character);
				} catch (Exception e) {
					LOG.error("Exception caught: ", e);
				}
			}
		}

		private void requestChar() {
			LOG.debug("Requesting character from async IO thread.");
			synchronized (_sema) {
				LOG.debug("Run loop got monitor.");
				if (_requestingChar)
					return;
				_requestingChar = true;
				_sema.notifyAll();
			}
		}
	}

	public void start() {
		_iothread = new IOThread();
		_iothread.start();

		while (!_exitRequested) {
			try {
				pollEvents();
				if (_exitRequested) {
					LOG.debug("Shut down event loop.");
					return;
				}
				_primaryResponder.refresh();
				_iothread.requestChar();
			} catch (Exception e) {
				LOG.error("Exception caught: ", e);
			}
		}
	}

	public IInputRecognizer getPrimaryResponder() {
		return _primaryResponder;
	}

	public void setPrimaryResponder(Window _primaryResponder) {
		this._primaryResponder = _primaryResponder;
	}

	public void exit() {
		LOG.debug("Requesting runloop shutdown.");
		synchronized (_sema) {
			_exitRequested = true;
			_sema.notify();
		}
		LOG.debug("Runloop shutdown finished.");
	}
}
