/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.responder;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.ui.screen.Screen;
import org.fisked.ui.window.IWindowManager;
import org.fisked.util.concurrency.IRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLoop implements IRunner {
	private final static Logger LOG = LoggerFactory.getLogger(EventLoop.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(EventLoop.class);
	private final BlockingQueue<Runnable> _queue = new ArrayBlockingQueue<>(1024);
	private IOThread _iothread;
	private volatile boolean _exitRequested;

	@Override
	public void run(Runnable runnable) {
		_queue.add(runnable);
	}

	private Screen getPrimaryScreen() {
		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			return wmBC.getBehavior().getPrimaryScreen();
		} catch (Exception e) {
			LOG.error("Exception getting primary responder: ", e);
			throw new RuntimeException(e);
		}
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
			IInputResponder responder = getPrimaryScreen();
			RecognitionState state = responder.recognizesInput(postponedStart);
			switch (state) {
			case Recognized:
				responder.onRecognize();
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
		} catch (Exception e) {
			LOG.error("Event loop caught exception: ", e);
		}
		while ((runnable = _queue.poll()) != null) {
			runnable.run();
		}
	}

	private Event postponedStart = null;
	private Event postponedEnd = null;
	private final Object _sema = new Object();

	private int getNextChar() throws IOException {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			return consoleBC.getBehavior().getChar();
		} catch (Exception e) {
			LOG.error("Exception in reading char: ", e);
			throw new RuntimeException(e);
		}
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
				getPrimaryScreen().fullRedraw();
				_iothread.requestChar();
			} catch (Exception e) {
				LOG.error("Exception caught: ", e);
			}
		}
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
