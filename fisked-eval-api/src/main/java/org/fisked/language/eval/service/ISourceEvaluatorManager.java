package org.fisked.language.eval.service;

import java.io.File;

public interface ISourceEvaluatorManager {
	void addEvaluator(SourceEvaluatorInformation evalInfo);

	void removeEvaluator(SourceEvaluatorInformation evalInfo);

	interface ISourceEvaluatorCallback {
		void call(ISourceEvaluator evaluator);
	}

	void getEvaluator(String language, ISourceEvaluatorCallback callbak);

	void getEvaluator(File file, ISourceEvaluatorCallback callback);
}
