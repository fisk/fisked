package org.fisked.language.eval.service;

import java.util.List;

public class SourceEvaluatorInformation {
	private final String _language;
	private final List<String> _fileEndings;

	public SourceEvaluatorInformation(String language, List<String> fileEndings) {
		_language = language;
		_fileEndings = fileEndings;
	}

	public String getLanguage() {
		return _language;
	}

	public List<String> getFileEndings() {
		return _fileEndings;
	}
}
