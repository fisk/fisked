package org.fisked.language.eval.lisp;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = LispEvaluator.COMPONENT_NAME)
@Provides
public class LispEvaluator implements ISourceEvaluator {
	@ServiceProperty(name = "language", value = "lisp")
	private String _language;

	private final static Logger LOG = LoggerFactory.getLogger(LispEvaluator.class);
	public static final String COMPONENT_NAME = "LispEvaluator";
	@Requires(optional = true)
	private ISourceEvaluatorManager _manager;
	private SourceEvaluatorInformation _info;

	private ScriptEngine _engine;

	private ScriptEngine getEngine() {
		if (_engine == null) {
			_engine = new ScriptEngineManager().getEngineByExtension("lisp");
		}
		return _engine;
	}

	@Override
	public String evaluate(String val) {
		try {
			return getEngine().eval(val).toString();
		} catch (ScriptException e) {
			return e.getMessage();
		}
	}

	@Bind
	public void bindManager(ISourceEvaluatorManager manager) {
		LOG.debug("Adding evaluator for lisp to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("cl");
		fileExtensions.add("lisp");
		_info = new SourceEvaluatorInformation("lisp", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Unbind
	public void unbindManager(ISourceEvaluatorManager manager) {
		_manager.removeEvaluator(_info);
	}
}
