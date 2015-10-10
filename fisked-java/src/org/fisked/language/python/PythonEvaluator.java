package org.fisked.language.python;

import org.fisked.language.ISourceEvaluator;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class PythonEvaluator implements ISourceEvaluator {
	private PythonInterpreter _interpreter = new PythonInterpreter();

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
