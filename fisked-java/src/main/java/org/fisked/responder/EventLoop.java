package org.fisked.responder;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.fisked.buffer.drawing.Window;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.services.ServiceManager;

public class EventLoop {
	private final BlockingQueue<Runnable> _queue = new ArrayBlockingQueue<>(1024);
	private Window _primaryResponder;
	private Thread _iothread;

	public void run(Runnable runnable) {
		_queue.add(runnable);
	}

	private void sendChar(int nextChar) {
		run(() -> {
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
		Runnable runnable;
		try {
			runnable = _queue.take();
			runnable.run();
		} catch (InterruptedException e) {
		}
		while ((runnable = _queue.poll()) != null) {
			runnable.run();
		}
	}

	private Event postponedStart = null;
	private Event postponedEnd = null;

	private int getNextChar() {
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();

		do {
			try {
				return cs.getChar();
			} catch (IOException e) {
			}
		} while (true);
	}

	public void start() {
		_iothread = new Thread() {
			@Override
			public void run() {
				while (true) {
					sendChar(getNextChar());
				}
			}
		};
		_iothread.start();

		while (true) {
			pollEvents();
			_primaryResponder.refresh();
		}
	}

	public IInputRecognizer getPrimaryResponder() {
		return _primaryResponder;
	}

	public void setPrimaryResponder(Window _primaryResponder) {
		this._primaryResponder = _primaryResponder;
	}
}
