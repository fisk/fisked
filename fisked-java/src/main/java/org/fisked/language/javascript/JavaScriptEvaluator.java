package org.fisked.language.javascript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.fisked.language.service.ISourceEvaluator;

public class JavaScriptEvaluator implements ISourceEvaluator {
	private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	@Override
	public String evaluate(String val) {
		try {
			return engine.eval(val).toString();
		} catch (ScriptException e) {
			return e.getMessage();
		}
	}
}
