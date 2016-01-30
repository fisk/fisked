package org.fisked.services;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.language.eval.service.ISourceEvaluatorManager;
import org.fisked.launcher.service.ILauncherService;

@Component(immediate = true, publicFactory = false)
@Instantiate(name = ComponentManager.COMPONENT_NAME)
public class ComponentManager {
	public static final String COMPONENT_NAME = "ComponentManager";
	private volatile static ComponentManager _singleton;

	@Requires
	private ISourceEvaluatorManager _sourceEvalManager;

	@Requires
	private ILauncherService _launcherService;

	@Validate
	public void start() {
		_singleton = this;
	}

	@Invalidate
	public void stop() {
		_singleton = null;
	}

	public static ComponentManager getInstance() {
		return _singleton;
	}

	public ISourceEvaluatorManager getSourceEvalManager() {
		return _sourceEvalManager;
	}

	public ILauncherService getLauncherService() {
		return _launcherService;
	}
}
