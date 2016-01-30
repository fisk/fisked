package org.fisked.language.eval.service;

import java.io.File;

public interface ISourceEvaluatorManager {
	void addEvaluator(SourceEvaluatorInformation evalInfo);

	void removeEvaluator(SourceEvaluatorInformation evalInfo);

	ISourceEvaluator getEvaluator(String language);

	ISourceEvaluator getEvaluator(File file);
}
