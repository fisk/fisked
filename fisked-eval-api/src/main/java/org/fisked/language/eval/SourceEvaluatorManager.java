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
package org.fisked.language.eval;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.language.eval.service.ISourceEvaluator;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.language.eval.service.SourceEvaluatorInformation;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = SourceEvaluatorManager.COMPONENT_NAME)
@Provides(specifications = ISourceEvaluatorManager.class)
public class SourceEvaluatorManager implements ISourceEvaluatorManager {
	private final static Logger LOG = LoggerFactory.getLogger(SourceEvaluatorManager.class);
	public static final String COMPONENT_NAME = "SourceEvaluatorManager";

	public SourceEvaluatorManager() {

	}

	private final Map<String, SourceEvaluatorInformation> _languageMap = new ConcurrentHashMap<>();
	private final Map<String, SourceEvaluatorInformation> _fileExtensionMap = new ConcurrentHashMap<>();

	private ServiceReference<ISourceEvaluator> getService(String language) {
		BundleContext context = FrameworkUtil.getBundle(SourceEvaluatorManager.class).getBundleContext();
		try {
			Collection<ServiceReference<ISourceEvaluator>> refs = context.getServiceReferences(ISourceEvaluator.class,
					"(language=" + language + ")");
			if (refs.size() != 1) {
				throw new RuntimeException("Language providers for " + language + ": " + refs.size());
			}
			return refs.iterator().next();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addEvaluator(SourceEvaluatorInformation evalInfo) {
		LOG.debug("Manager " + this + " added evaluator for " + evalInfo.getLanguage());
		_languageMap.put(evalInfo.getLanguage(), evalInfo);
		for (String fileExtension : evalInfo.getFileEndings()) {
			_fileExtensionMap.put(fileExtension, evalInfo);
		}
	}

	@Override
	public void removeEvaluator(SourceEvaluatorInformation evalInfo) {
		_languageMap.remove(evalInfo.getLanguage(), evalInfo);
		for (String fileExtension : evalInfo.getFileEndings()) {
			_fileExtensionMap.remove(fileExtension, evalInfo);
		}
	}

	@Override
	public void getEvaluator(File file, ISourceEvaluatorCallback callback) {
		if (!file.exists())
			return;
		String extension = FilenameUtils.getExtension(file.getName());
		SourceEvaluatorInformation evalInfo = _fileExtensionMap.get(extension);
		if (evalInfo == null)
			return;

		LOG.debug("Manager " + this + " queried evaluator for " + evalInfo.getLanguage());
		BundleContext context = FrameworkUtil.getBundle(SourceEvaluatorManager.class).getBundleContext();
		ServiceReference<ISourceEvaluator> ref = getService(evalInfo.getLanguage());
		LOG.debug("Manager found evaluator.");
		ISourceEvaluator service = context.getService(ref);
		callback.call(service);
		context.ungetService(ref);
	}

	@Override
	public void getEvaluator(String language, ISourceEvaluatorCallback callback) {
		SourceEvaluatorInformation evalInfo = _languageMap.get(language);
		if (evalInfo == null)
			return;

		LOG.debug("Manager " + this + " queried evaluator for " + evalInfo.getLanguage());
		BundleContext context = FrameworkUtil.getBundle(SourceEvaluatorManager.class).getBundleContext();
		ServiceReference<ISourceEvaluator> ref = getService(evalInfo.getLanguage());
		LOG.debug("Manager found evaluator.");
		ISourceEvaluator service = context.getService(ref);
		callback.call(service);
		context.ungetService(ref);
	}
}
