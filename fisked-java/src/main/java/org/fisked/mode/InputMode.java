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
package org.fisked.mode;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.mode.responder.TextInputResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;

public class InputMode extends AbstractMode {

	private final TextInputResponder _textInputResponder;

	public InputMode(BufferWindow window) {
		super(window);
		addResponder(new NormalModeSwitchResponder(_window));
		_textInputResponder = new TextInputResponder(_window);
		addResponder(_textInputResponder);
		window.getBuffer().pushUndoScope();
	}

	@Override
	public Face getModelineFace() {
		return new Face(Color.RED, Color.WHITE);
	}

	@Override
	public void activate() {
		changeCursor(CURSOR_VERTICAL_BAR);
	}

	@Override
	public void deactivate() {
		getWindow().getBuffer().popUndoScope();
	}

	@Override
	public String getModeName() {
		return "insert";
	}

}
