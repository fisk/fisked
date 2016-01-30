package org.fisked.language.eval;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.language.eval.service.ISourceEvaluator;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.language.eval.service.SourceEvaluatorInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = SourceEvaluatorManager.COMPONENT_NAME)
@Provides(specifications = ISourceEvaluatorManager.class)
public class SourceEvaluatorManager implements ISourceEvaluatorManager {
	private final static Logger LOG = LoggerFactory.getLogger(SourceEvaluatorManager.class);
	public static final String COMPONENT_NAME = "SourceEvaluatorManager";

	public SourceEvaluatorManager() {

	}

	private final Map<String, SourceEvaluatorInformation> _languageMap = new ConcurrentHashMap<>();
	private final Map<String, SourceEvaluatorInformation> _fileExtensionMap = new ConcurrentHashMap<>();

	@Override
	public void addEvaluator(SourceEvaluatorInformation evalInfo) {
		LOG.debug("Manager " + this + " added evaluator for " + evalInfo.getLanguage());
		_languageMap.put(evalInfo.getLanguage(), evalInfo);
		for (String fileExtension : evalInfo.getFileEndings()) {
			_fileExtensionMap.put(fileExtension, evalInfo);
		}
	}

	@Override
	public void removeEvaluator(SourceEvaluatorInformation evalInfo) {
		_languageMap.remove(evalInfo.getLanguage(), evalInfo);
		for (String fileExtension : evalInfo.getFileEndings()) {
			_fileExtensionMap.remove(fileExtension, evalInfo);
		}
	}

	@Override
	public ISourceEvaluator getEvaluator(File file) {
		if (!file.exists())
			return null;
		String extension = FilenameUtils.getExtension(file.getName());
		SourceEvaluatorInformation evalInfo = _fileExtensionMap.get(extension);
		if (evalInfo == null)
			return null;
		return evalInfo.getEvaluator();
	}

	@Override
	public ISourceEvaluator getEvaluator(String language) {
		SourceEvaluatorInformation evalInfo = _languageMap.get(language);
		LOG.debug("Manager " + this + " queried evaluator for " + language);
		if (evalInfo == null)
			return null;
		return evalInfo.getEvaluator();
	}
}
