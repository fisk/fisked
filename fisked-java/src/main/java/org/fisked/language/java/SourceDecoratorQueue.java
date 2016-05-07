/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
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
