package org.fisked.language.ruby;

import org.fisked.language.ISourceEvaluator;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyEvaluator implements ISourceEvaluator {
	private final Ruby _runtime = Ruby.newInstance();

	@Override
	public String evaluate(String val) {
		try {
			IRubyObject result = _runtime.evalScriptlet(val);
			return result.asJavaString();
		} catch (Throwable e) {
			return e.getMessage();
		}
	}
}
