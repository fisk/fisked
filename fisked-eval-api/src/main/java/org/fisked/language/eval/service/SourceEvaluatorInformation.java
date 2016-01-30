package org.fisked.language.eval.service;

import java.util.List;

public class SourceEvaluatorInformation {
	private final String _language;
	private final List<String> _fileEndings;
	private final ISourceEvaluator _evaluator;

	public SourceEvaluatorInformation(ISourceEvaluator evaluator, String language, List<String> fileEndings) {
		_language = language;
		_fileEndings = fileEndings;
		_evaluator = evaluator;
	}

	public String getLanguage() {
		return _language;
	}

	public List<String> getFileEndings() {
		return _fileEndings;
	}

	public ISourceEvaluator getEvaluator() {
		return _evaluator;
	}
}
