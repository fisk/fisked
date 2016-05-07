package org.fisked.renderingengine;

import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.util.shell.ShellCommandExecution;
import org.fisked.util.shell.ShellCommandExecution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacClipboardService implements IClipboardService {
	private final static Logger LOG = LoggerFactory.getLogger(MacClipboardService.class);

	@Override
	public String getClipboard() {
		LOG.debug("Get mac clipboard");
		ShellCommandExecution sh = new ShellCommandExecution("pbpaste");
		ExecutionResult result = sh.executeSync();
		LOG.debug("Got mac clipboard: " + result.getResult());
		return result.getResult();
	}

	@Override
	public void setClipboard(String value) {
		LOG.debug("Set mac clipboard: " + value);
		ShellCommandExecution sh = new ShellCommandExecution("pbcopy");
		sh.setInputString(value);
		sh.executeSync();
		LOG.debug("Set mac clipboard");
	}

}
