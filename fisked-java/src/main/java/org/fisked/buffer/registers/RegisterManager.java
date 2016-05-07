/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
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
package org.fisked.buffer.registers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.renderingengine.service.models.selection.SelectionMode;
import org.fisked.renderingengine.service.models.selection.TextSelection;
import org.fisked.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterManager {
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(RegisterManager.class);
	private final static Logger LOG = LoggerFactory.getLogger(RegisterManager.class);

	public static RegisterManager getInstance() {
		return Singleton.getInstance(RegisterManager.class);
	}

	private final Map<Character, TextSelection> _map = new ConcurrentHashMap<>();

	public TextSelection getRegister(char reg) {
		if (reg == SYSTEM_REGISTER) {
			return getClipboard();
		} else {
			return _map.get(reg);
		}
	}

	public void setRegister(char reg, TextSelection str) {
		if (reg == SYSTEM_REGISTER) {
			setClipboard(str);
		} else {
			_map.put(reg, str);
		}
	}

	private void setClipboard(TextSelection text) {
		try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
				.getBehaviorConnection(IClipboardService.class).get()) {
			LOG.debug("Setting clipboard: " + text);
			clipboardBC.getBehavior().setClipboard(text.getText());
			LOG.debug("Clipboard success.");
		} catch (Exception e) {
			LOG.error("Exception in clipboard: ", e);
			_map.put(SYSTEM_REGISTER, text);
		}
	}

	private TextSelection getClipboard() {
		try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
				.getBehaviorConnection(IClipboardService.class).get()) {
			LOG.debug("Getting clipboard");
			String result = clipboardBC.getBehavior().getClipboard();
			LOG.debug("Clipboard success: " + result);
			return new TextSelection(SelectionMode.NORMAL_MODE, result);
		} catch (Exception e) {
			LOG.error("Exception in clipboard: ", e);
			return _map.get(SYSTEM_REGISTER);
		}
	}

	public static final char SYSTEM_REGISTER = '*';
	public static final char UNNAMED_REGISTER = '\0';
}
