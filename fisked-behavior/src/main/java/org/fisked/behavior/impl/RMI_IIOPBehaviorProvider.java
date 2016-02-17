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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMI_IIOPBehaviorProvider implements IBehaviorProvider {
	final IBehaviorProvider _parent;
	private final static Logger LOG = LoggerFactory.getLogger(RMI_IIOPBehaviorProvider.class);

	{
		ProcessBuilder processBuilder = new ProcessBuilder("orbd", "-ORBInitialPort", "1050");
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
			LOG.debug("Not RMI class.");
			if (_parent != null) {
				LOG.debug("Calling up.");
				return _parent.getBehaviorConnection(callerClass, targetClass);
			} else {
				LOG.debug("Could not find behavior.");
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
			LOG.debug("Found behavior.");

			return CompletableFuture.completedFuture(new RMIIIOPConnection<T>(instance));
		} catch (Exception e) {
			LOG.debug("Error retrieving behavior.");
			if (_parent != null) {
				LOG.debug("Calling up.");
				return _parent.getBehaviorConnection(callerClass, targetClass);
			} else {
				LOG.debug("Could not find behavior.");
				return null;
			}
		}
	}
}
