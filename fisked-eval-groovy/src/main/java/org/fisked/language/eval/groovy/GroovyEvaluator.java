package org.fisked.language.eval.groovy;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyShell;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = GroovyEvaluator.COMPONENT_NAME)
@Provides
public class GroovyEvaluator implements ISourceEvaluator {
	@ServiceProperty(name = "language", value = "groovy")
	private String _language;

	private final static Logger LOG = LoggerFactory.getLogger(GroovyEvaluator.class);
	public static final String COMPONENT_NAME = "GroovyEvaluator";
	@Requires(optional = true)
	private ISourceEvaluatorManager _manager;
	private SourceEvaluatorInformation _info;

	private GroovyShell _engine;

	private GroovyShell getEngine() {
		if (_engine == null) {
			_engine = new GroovyShell();
		}
		return _engine;
	}

	@Override
	public String evaluate(String val) {
		try {
			Object result = getEngine().evaluate(val);
			return result.toString();
		} catch (Throwable e) {
			return e.getMessage();
		}
	}

	@Bind
	public void bindManager(ISourceEvaluatorManager manager) {
		LOG.debug("Adding evaluator for groovy to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("groovy");
		fileExtensions.add("gvy");
		_info = new SourceEvaluatorInformation("groovy", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Unbind
	public void unbindManager(ISourceEvaluatorManager manager) {
		_manager.removeEvaluator(_info);
	}
}
