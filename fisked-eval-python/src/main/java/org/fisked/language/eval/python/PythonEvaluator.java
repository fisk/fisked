package org.fisked.language.eval.python;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.language.eval.python.service.IPythonSourceEvaluator;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.language.eval.service.SourceEvaluatorInformation;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = PythonEvaluator.COMPONENT_NAME)
@Provides
public class PythonEvaluator implements IPythonSourceEvaluator {
	private final static Logger LOG = LoggerFactory.getLogger(PythonEvaluator.class);
	public static final String COMPONENT_NAME = "PythonEvaluator";
	@Requires
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

	@Validate
	public void start() {
		LOG.debug("Adding evaluator for python to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("py");
		_info = new SourceEvaluatorInformation(this, "python", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Invalidate
	public void end() {
		_manager.removeEvaluator(_info);
	}
}
