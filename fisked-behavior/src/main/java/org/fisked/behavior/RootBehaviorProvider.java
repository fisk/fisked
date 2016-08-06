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
package org.fisked.behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.fisked.behavior.impl.CompositionBehaviorProvider;
import org.fisked.behavior.impl.NormalBehaviorProvider;
import org.fisked.behavior.impl.OSGiBehaviorProvider;
import org.fisked.behavior.impl.RMI_IIOPBehaviorProvider;
import org.fisked.util.Singleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class RootBehaviorProvider {
	class SourceCompletions<C, T> {
		Map<Class<C>, List<CompletableFuture<IBehaviorConnection<T>>>> _futures = new HashMap<>();
	}

	private final Map<Class<?>, SourceCompletions<?, ?>> _sourceCompletions = new HashMap<>();

	public synchronized <C, T> void registerFuture(Class<C> callerClass, Class<T> targetClass,
			CompletableFuture<IBehaviorConnection<T>> future) {
		@SuppressWarnings("unchecked")
		SourceCompletions<C, T> completions = (SourceCompletions<C, T>) _sourceCompletions.get(callerClass);
		if (completions == null) {
			completions = new SourceCompletions<C, T>();
			_sourceCompletions.put(callerClass, completions);
		}

		List<CompletableFuture<IBehaviorConnection<T>>> list = completions._futures.get(targetClass);
		if (list == null) {
			list = new ArrayList<>();
			completions._futures.put(callerClass, list);
		}

		list.add(future);
	}

	private final Map<Class<?>, IBehaviorProvider> _classToProvider = new HashMap<>();
	private final Map<Bundle, IBehaviorProvider> _bundleToProvider = new HashMap<>();

	public synchronized <C> IBehaviorProvider getBehaviorProvider(Class<C> callerClass) {
		IBehaviorProvider provider = _classToProvider.get(callerClass);
		Bundle bundle = FrameworkUtil.getBundle(callerClass);
		if (provider != null) {
			if (bundle != null) {
				IBehaviorProvider bundleProvider = _bundleToProvider.get(bundle);
				CompositionBehaviorProvider intent = new CompositionBehaviorProvider();
				intent.addProvider("bundle", bundleProvider);
				intent.addProvider("class", provider);
				return intent;
			}
			return provider;
		}

		OSGiBehaviorProvider osgi = new OSGiBehaviorProvider();
		RMI_IIOPBehaviorProvider rmi_iiop = new RMI_IIOPBehaviorProvider(osgi);
		NormalBehaviorProvider normal = new NormalBehaviorProvider(rmi_iiop);
		CompositionBehaviorProvider intent = new CompositionBehaviorProvider();
		intent.addProvider("osgi", osgi);
		intent.addProvider("rmi_iiop", rmi_iiop);
		intent.addProvider("normal", normal);

		_bundleToProvider.put(bundle, intent);
		return intent;
	}

	public synchronized <C, T> void setCallerBehavior(Class<C> callerClass, Class<T> targetClass, T behavior) {
		CompositionBehaviorProvider provider = (CompositionBehaviorProvider) getBehaviorProvider(callerClass);
		NormalBehaviorProvider normal = (NormalBehaviorProvider) provider.getProvider("normal");
		normal.addBehavior(targetClass, behavior);

		@SuppressWarnings("unchecked")
		SourceCompletions<C, T> completions = (SourceCompletions<C, T>) _sourceCompletions.get(callerClass);
		if (completions == null) {
			completions = new SourceCompletions<C, T>();
			_sourceCompletions.put(callerClass, completions);
		}

		List<CompletableFuture<IBehaviorConnection<T>>> list = completions._futures.get(targetClass);
		if (list == null) {
			list = new ArrayList<>();
			completions._futures.put(callerClass, list);
		}

		for (CompletableFuture<IBehaviorConnection<T>> future : list) {
			future.complete(new IBehaviorConnection<T>() {
				@Override
				public void close() throws Exception {

				}

				@Override
				public T getBehavior() {
					return behavior;
				}
			});
		}
	}

	public static RootBehaviorProvider getInstance() {
		return Singleton.getInstance(RootBehaviorProvider.class);
	}

}
