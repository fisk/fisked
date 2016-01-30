package org.fisked.language.eval.groovy;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.language.eval.groovy.service.IGroovySourceEvaluator;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.language.eval.service.SourceEvaluatorInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyShell;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = GroovyEvaluator.COMPONENT_NAME)
@Provides
public class GroovyEvaluator implements IGroovySourceEvaluator {
	private final static Logger LOG = LoggerFactory.getLogger(GroovyEvaluator.class);
	public static final String COMPONENT_NAME = "RubyEvaluator";
	@Requires
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

	@Validate
	public void start() {
		LOG.debug("Adding evaluator for ruby to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("groovy");
		fileExtensions.add("gvy");
		_info = new SourceEvaluatorInformation(this, "groovy", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Invalidate
	public void end() {
		_manager.removeEvaluator(_info);
	}
}
