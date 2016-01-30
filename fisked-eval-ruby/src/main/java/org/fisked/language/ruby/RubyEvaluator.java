package org.fisked.language.ruby;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.language.ruby.service.IRubySourceEvaluator;
import org.fisked.language.service.ISourceEvaluatorManager;
import org.fisked.language.service.SourceEvaluatorInformation;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = RubyEvaluator.COMPONENT_NAME)
@Provides
public class RubyEvaluator implements IRubySourceEvaluator {
	private final static Logger LOG = LoggerFactory.getLogger(RubyEvaluator.class);
	public static final String COMPONENT_NAME = "RubyEvaluator";
	private final Ruby _runtime = Ruby.newInstance();
	@Requires
	private ISourceEvaluatorManager _manager;
	private SourceEvaluatorInformation _info;

	@Override
	public String evaluate(String val) {
		try {
			IRubyObject result = _runtime.evalScriptlet(val);
			return result.asJavaString();
		} catch (Throwable e) {
			return e.getMessage();
		}
	}

	@Validate
	public void start() {
		LOG.debug("Adding evaluator for ruby to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("rb");
		_info = new SourceEvaluatorInformation(this, "ruby", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Invalidate
	public void end() {
		_manager.removeEvaluator(_info);
	}
}
