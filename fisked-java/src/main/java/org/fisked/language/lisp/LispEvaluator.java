package org.fisked.language.lisp;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.fisked.language.ISourceEvaluator;

public class LispEvaluator implements ISourceEvaluator {
	private final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("lisp");

	@Override
	public String evaluate(String val) {
		try {
			return engine.eval(val).toString();
		} catch (ScriptException e) {
			return e.getMessage();
		}
	}
}
