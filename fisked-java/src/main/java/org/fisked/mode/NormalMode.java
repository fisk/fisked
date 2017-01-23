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
package org.fisked.mode;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.CursorManagerResponder;
import org.fisked.mode.responder.DeleteLineResponder;
import org.fisked.mode.responder.InputModeSwitchResponder;
import org.fisked.mode.responder.MotionActionResponder;
import org.fisked.mode.responder.SearchTextResponder;
import org.fisked.mode.responder.VisualModeSwitchResponder;
import org.fisked.project.ProjectResponder;
import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;
import org.fisked.text.TextNavigator;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.util.models.Color;
import org.fisked.util.models.Face;
import org.fisked.util.models.selection.SelectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalMode extends AbstractMode {
	private final static Logger LOG = LoggerFactory.getLogger(NormalMode.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(NormalMode.class);

	public NormalMode(BufferWindow window) {
		super(window, SelectionMode.INVALID_MODE, CURSOR_UNDERLINE);
		addResponder(new CommandInputResponder(_window));
		addResponder(new SearchTextResponder(_window));
		addResponder(new InputModeSwitchResponder(_window));
		addResponder(new VisualModeSwitchResponder(_window));
		addResponder(new BasicNavigationResponder(_window));
		addResponder(new CursorManagerResponder(_window));
		addResponder(new MotionActionResponder(_window));
		addResponder(nextEvent -> {
			if (nextEvent.isCharacter('p')) {
				try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
						.getBehaviorConnection(IClipboardService.class).get()) {
					_window.getBuffer().appendStringAtPointLogged(clipboardBC.getBehavior().getClipboard());
				} catch (Exception e) {
					LOG.error("Exception in clipboard: ", e);
				}
				_window.switchToNormalMode();
				_window.setNeedsFullRedraw();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder(nextEvent -> {
			if (nextEvent.isCharacter('P')) {
				TextNavigator navigator = new TextNavigator(_window);
				navigator.moveLeft();
				try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
						.getBehaviorConnection(IClipboardService.class).get()) {
					_window.getBuffer().appendStringAtPointLogged(clipboardBC.getBehavior().getClipboard());
				} catch (Exception e) {
					LOG.error("Exception in clipboard: ", e);
				}
				_window.switchToNormalMode();
				_window.setNeedsFullRedraw();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder(nextEvent -> {
			return EventRecognition.matchesExact(nextEvent, "u");
		}, () -> {
			_window.getBuffer().undo();
			_window.setNeedsFullRedraw();
		});
		addResponder(nextEvent -> {
			return nextEvent.isControlChar('r') ? RecognitionState.Recognized : RecognitionState.NotRecognized;
		}, () -> {
			_window.getBuffer().redo();
			_window.setNeedsFullRedraw();
		});
		addResponder(new DeleteLineResponder(_window));
		addResponder(new ProjectResponder(_window));
	}

	@Override
	public Face getModelineFace() {
		return new Face(Color.MAGENTA, Color.WHITE);
	}

	@Override
	public void activate() {
		super.activate();
	}

	@Override
	public String getModeName() {
		return "normal";
	}

}
