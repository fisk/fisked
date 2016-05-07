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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fisked.behavior.impl.CompositionBehaviorProvider;
import org.fisked.behavior.impl.NormalBehaviorProvider;
import org.fisked.behavior.impl.OSGiBehaviorProvider;
import org.fisked.behavior.impl.RMI_IIOPBehaviorProvider;
import org.fisked.util.Singleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class RootBehaviorProvider {
	private final Map<BundleContext, CompositionBehaviorProvider> _map = new ConcurrentHashMap<>();

	public <C> CompositionBehaviorProvider getBehaviorProvider(Class<C> callerClass) {
		Bundle bundle = FrameworkUtil.getBundle(callerClass);
		BundleContext context = bundle.getBundleContext();

		CompositionBehaviorProvider provider = _map.get(context);
		if (provider != null) {
			return provider;
		}

		OSGiBehaviorProvider osgi = new OSGiBehaviorProvider();
		RMI_IIOPBehaviorProvider rmi_iiop = new RMI_IIOPBehaviorProvider(osgi);
		NormalBehaviorProvider normal = new NormalBehaviorProvider(rmi_iiop);
		CompositionBehaviorProvider intent = new CompositionBehaviorProvider();
		intent.addProvider("osgi", osgi);
		intent.addProvider("rmi_iiop", rmi_iiop);
		intent.addProvider("normal", normal);

		provider = _map.put(context, intent);
		if (provider != null) {
			return provider;
		}
		return intent;
	}

	public static RootBehaviorProvider getInstance() {
		return Singleton.getInstance(RootBehaviorProvider.class);
	}

}
