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
package org.fisked.behavior.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalBehaviorProvider implements IBehaviorProvider {
	private IBehaviorProvider _parent;
	private final Map<Class<?>, Object> _map = new ConcurrentHashMap<>();
	private final static Logger LOG = LoggerFactory.getLogger(NormalBehaviorProvider.class);

	public class NormalBehaviorCaller<T> implements IBehaviorConnection<T> {
		private final T _behavior;

		public NormalBehaviorCaller(T behavior) {
			_behavior = behavior;
		}

		@Override
		public void close() throws Exception {

		}

		@Override
		public T getBehavior() {
			return _behavior;
		}

	}

	public NormalBehaviorProvider() {

	}

	public NormalBehaviorProvider(IBehaviorProvider parent) {
		_parent = parent;
	}

	@Override
	public <C, T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		Object mapResult = _map.get(targetClass);
		if (mapResult != null) {
			T service = targetClass.cast(mapResult);
			LOG.info("Found normal behavior.");
			return CompletableFuture.completedFuture(new NormalBehaviorCaller<T>(service));
		}
		if (_parent != null) {
			LOG.info("Calling up.");
			return _parent.getBehaviorConnection(callerClass, targetClass);
		} else {
			LOG.info("No behavior found.");
			return null;
		}
	}

	public <T> void addBehavior(Class<T> type, T behavior) {
		_map.put(type, behavior);
	}

}
