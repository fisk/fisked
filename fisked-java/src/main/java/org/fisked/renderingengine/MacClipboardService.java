/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
