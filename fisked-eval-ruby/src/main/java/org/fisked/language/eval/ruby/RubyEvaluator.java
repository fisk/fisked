package org.fisked.language.eval.ruby;

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
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = RubyEvaluator.COMPONENT_NAME)
@Provides
public class RubyEvaluator implements ISourceEvaluator {
	@ServiceProperty(name = "language", value = "ruby")
	private String _language;

	private final static Logger LOG = LoggerFactory.getLogger(RubyEvaluator.class);
	public static final String COMPONENT_NAME = "RubyEvaluator";
	private Ruby _runtime;
	@Requires(optional = true)
	private ISourceEvaluatorManager _manager;
	private SourceEvaluatorInformation _info;

	private Ruby getRuntime() {
		if (_runtime == null) {
			_runtime = Ruby.newInstance();
		}
		return _runtime;
	}

	@Override
	public String evaluate(String val) {
		try {
			IRubyObject result = getRuntime().evalScriptlet(val);
			return result.asJavaString();
		} catch (Throwable e) {
			return e.getMessage();
		}
	}

	@Bind
	public void bindManager(ISourceEvaluatorManager manager) {
		LOG.debug("Adding evaluator for ruby to manager: " + _manager);
		List<String> fileExtensions = new ArrayList<>();
		fileExtensions.add("rb");
		_info = new SourceEvaluatorInformation("ruby", fileExtensions);
		_manager.addEvaluator(_info);
	}

	@Unbind
	public void unbindManager(ISourceEvaluatorManager manager) {
		_manager.removeEvaluator(_info);
	}
}
