package org.fisked.language;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.fisked.language.javascript.JavaScriptEvaluator;
import org.fisked.language.lisp.LispEvaluator;
import org.fisked.language.python.PythonEvaluator;
import org.fisked.language.ruby.RubyEvaluator;

public class SourceEvaluatorManager {
	private static SourceEvaluatorManager _instance = null;

	public static SourceEvaluatorManager getInstance() {
		if (_instance != null)
			return _instance;
		synchronized (SourceEvaluatorManager.class) {
			if (_instance != null)
				return _instance;
			_instance = new SourceEvaluatorManager();
		}
		return _instance;
	}

	private ISourceEvaluator _ruby;
	private ISourceEvaluator _python;
	private ISourceEvaluator _javaScript;
	private ISourceEvaluator _lisp;

	public ISourceEvaluator getEvaluator(String language) {
		if (language.equals("ruby")) {
			if (_ruby != null)
				return _ruby;
			synchronized (this) {
				if (_ruby != null)
					return _ruby;
				_ruby = new RubyEvaluator();
			}
			return _ruby;
		}
		if (language.equals("python")) {
			if (_python != null)
				return _python;
			synchronized (this) {
				if (_python != null)
					return _python;
				_python = new PythonEvaluator();
			}
			return _python;
		}
		if (language.equals("javascript")) {
			if (_javaScript != null)
				return _javaScript;
			synchronized (this) {
				if (_javaScript != null)
					return _javaScript;
				_javaScript = new JavaScriptEvaluator();
			}
			return _javaScript;
		}
		if (language.equals("lisp")) {
			if (_lisp != null)
				return _lisp;
			synchronized (this) {
				if (_lisp != null)
					return _lisp;
				_lisp = new LispEvaluator();
			}
			return _lisp;
		}
		return null;
	}

	public ISourceEvaluator getEvaluator(File file) {
		if (!file.exists())
			return null;
		String extension = FilenameUtils.getExtension(file.getName());
		if (extension.equals("py"))
			return getEvaluator("python");
		if (extension.equals("rb"))
			return getEvaluator("ruby");
		if (extension.equals("js"))
			return getEvaluator("javascript");
		if (extension.equals("lisp"))
			return getEvaluator("lisp");
		return null;
	}
}
