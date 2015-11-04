package org.fisked.language;

import org.fisked.language.python.PythonEvaluator;
import org.fisked.language.ruby.RubyEvaluator;

public class SourceEvaluatorManager {
	private static SourceEvaluatorManager _instance = null;
	public static SourceEvaluatorManager getInstance() {
		if (_instance != null) return _instance;
		synchronized (SourceEvaluatorManager.class) {
			if (_instance != null) return _instance;
			_instance = new SourceEvaluatorManager();
		}
		return _instance;
	}

	private ISourceEvaluator _ruby;
	private ISourceEvaluator _python;
	public ISourceEvaluator getEvaluator(String language) {
		if (language.equals("ruby")) {
			if (_ruby != null) return _ruby;
			synchronized (this) {
				if (_ruby != null) return _ruby;
				_ruby = new RubyEvaluator();
			}
			return _ruby;
		}
		if (language.equals("python")) {
			if (_python != null) return _python;
			synchronized (this) {
				if (_python != null) return _python;
				_python = new PythonEvaluator();
			}
			return _python;
		}
		return null;
	}
}
