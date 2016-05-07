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
package org.fisked.language.eval.python;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.fisked.language.eval.service.ISourceEvaluator;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.language.eval.service.SourceEvaluatorInformation;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = PythonEvaluator.COMPONENT_NAME)
@Provides
public class PythonEvaluator implements ISourceEvaluator {
	@ServiceProperty(name = "language", value = "python")
	private String _language;

	private final static Logger LOG = LoggerFactory.getLogger(PythonEvaluator.class);
	public static final String COMPONENT_NAME = "PythonEvaluator";
	@Requires(optional = true)
	private ISourceEvaluatorManager _manager;
	private SourceEvaluatorInformation _info;

	private PythonInterpreter _interpreter;

	private PythonInterpreter getInterpreter() {
		if (_interpreter == null) {
			_interpreter = new PythonInterpreter();
		}
		return _interpreter;
	}

	@Override
	public String evaluate(String val) {
		try {
			LOG.debug("Evaluating python code: " + val);
			PyObject result = getInterpreter().eval(val);
			return result.asStringOrNull();
		} catch (Throwable e) {
			return e.getMessage();
		}
	}

	@Bind
	public void bindManager(ISourceEvaluatorManager manager) {
		LOG.debug("Adding evaluator for python to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("py");
		_info = new SourceEvaluatorInformation("python", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Unbind
	public void unbindManager(ISourceEvaluatorManager manager) {
		_manager.removeEvaluator(_info);
	}
}
