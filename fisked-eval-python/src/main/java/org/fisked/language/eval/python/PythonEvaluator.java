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
