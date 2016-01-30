package org.fisked.language.python;

import org.fisked.language.service.ISourceEvaluator;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class PythonEvaluator implements ISourceEvaluator {
	private final PythonInterpreter _interpreter = new PythonInterpreter();

	@Override
	public String evaluate(String val) {
		try {
			PyObject result = _interpreter.eval(val);
			return result.asString();
		} catch (Throwable e) {
			return e.getMessage();
		}
	}

}
