package org.fisked.renderingengine;

import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.shell.ShellCommandExecution;
import org.fisked.shell.ShellCommandExecution.ExecutionResult;

public class MacClipboardService implements IClipboardService {

	@Override
	public String getClipboard() {
		ShellCommandExecution sh = new ShellCommandExecution("pbpaste");
		ExecutionResult result = sh.executeSync();
		return result.getResult();
	}

	@Override
	public void setClipboard(String value) {
		ShellCommandExecution sh = new ShellCommandExecution("pbcopy");
		sh.setInputSource(value);
		sh.executeSync();
	}

}
