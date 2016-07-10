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

import java.io.IOException;
import java.rmi.Remote;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.fisked.behavior.IBehaviorConnection;
import org.fisked.behavior.IBehaviorProvider;
import org.fisked.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMI_IIOPBehaviorProvider implements IBehaviorProvider {
	final IBehaviorProvider _parent;
	private final static Logger LOG = LoggerFactory.getLogger(RMI_IIOPBehaviorProvider.class);

	{
		ProcessBuilder processBuilder = new ProcessBuilder("orbd", "-port", "1050", "-defaultdb",
				FileUtil.getFiskedFile("orb.db").getAbsolutePath());
		processBuilder.redirectErrorStream(true);
		try {
			processBuilder.start();
		} catch (IOException e) {
			LOG.error("Could not start orbd: ", e);
		}
	}

	public RMI_IIOPBehaviorProvider(IBehaviorProvider parent) {
		_parent = parent;
	}

	public RMI_IIOPBehaviorProvider() {
		this(null);
	}

	private class RMIIIOPConnection<T> implements IBehaviorConnection<T> {
		private final T _instance;

		public RMIIIOPConnection(T instance) {
			_instance = instance;
		}

		@Override
		public void close() throws Exception {
		}

		@Override
		public T getBehavior() {
			return _instance;
		}
	}

	@Override
	public <C, T> Future<IBehaviorConnection<T>> getBehaviorConnection(Class<C> callerClass, Class<T> targetClass) {
		if (!Remote.class.isAssignableFrom(targetClass)) {
			LOG.trace("Not RMI class.");
			if (_parent != null) {
				LOG.trace("Calling up.");
				return _parent.getBehaviorConnection(callerClass, targetClass);
			} else {
				LOG.trace("Could not find behavior.");
				return null;
			}
		}

		Context context;
		Object objref;
		T instance;

		try {
			context = new InitialContext();
			objref = context.lookup(targetClass.getName());
			instance = targetClass.cast(PortableRemoteObject.narrow(objref, targetClass));
			LOG.trace("Found behavior.");

			return CompletableFuture.completedFuture(new RMIIIOPConnection<T>(instance));
		} catch (Exception e) {
			LOG.debug("Error retrieving behavior.");
			if (_parent != null) {
				LOG.trace("Calling up.");
				return _parent.getBehaviorConnection(callerClass, targetClass);
			} else {
				LOG.trace("Could not find behavior.");
				return null;
			}
		}
	}
}
