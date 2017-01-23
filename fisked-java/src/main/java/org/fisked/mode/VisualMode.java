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

import org.fisked.buffer.Buffer;
import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.buffer.controller.FatTextSelection;
import org.fisked.buffer.controller.TextTransaction;
import org.fisked.buffer.registers.RegisterManager;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.mode.responder.SearchTextResponder;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.register.RegisterRecognizer;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.util.Wrapper;
import org.fisked.util.models.Color;
import org.fisked.util.models.Face;
import org.fisked.util.models.Range;
import org.fisked.util.models.selection.SelectionMode;
import org.fisked.util.models.selection.TextSelection;

public class VisualMode extends AbstractMode {
	private SelectionMode _mode;

	public SelectionMode getMode() {
		return _mode;
	}

	private void addCopyResponder() {
		RegisterRecognizer registerRecognizer = new RegisterRecognizer();
		addResponder(EventRecognition.builder().optional(registerRecognizer).require(EventRecognition.recognizer("y"))
				.build(() -> {
					StringBuilder builder = new StringBuilder();
					Wrapper<Integer> firstIndex = new Wrapper<>();
					_window.getBufferController().getFatTextSelections().forEach((FatTextSelection selection) -> {
						builder.append(selection);
					});
					TextSelection selection = new TextSelection(_mode, builder.toString());
					RegisterManager.getInstance().setRegister(registerRecognizer.getRegister(), selection);
					_window.getBufferController().collapseCursors(firstIndex.getValue());
					_window.switchToNormalMode();
				}));
	}

	private void addDeleteResponder() {
		RegisterRecognizer registerRecognizer = new RegisterRecognizer();
		addResponder(EventRecognition.builder().optional(registerRecognizer).require(EventRecognition.recognizer("d"))
				.build(() -> {
					Buffer buffer = _window.getBuffer();
					StringBuilder builder = new StringBuilder();
					_window.getBufferController().getFatTextSelections().forEach((FatTextSelection selection) -> {
						builder.append(selection);
					});
					TextSelection selectionText = new TextSelection(_mode, builder.toString());
					RegisterManager.getInstance().setRegister(registerRecognizer.getRegister(), selectionText);

					try (UndoScope us = buffer.createUndoScope()) {
						TextTransaction transaction = buffer.makeTextTransaction(true);
						transaction.executeDeleteSelection(_mode);
					}

					_window.switchToNormalMode();
				}));
	}

	private void addReplaceResponder() {
		RegisterRecognizer registerRecognizer = new RegisterRecognizer();
		addResponder(EventRecognition.builder().optional(registerRecognizer).require(EventRecognition.recognizer("c"))
				.build(() -> {
					Buffer buffer = _window.getBuffer();
					StringBuilder builder = new StringBuilder();
					_window.getBufferController().getFatTextSelections().forEach((FatTextSelection selection) -> {
						builder.append(selection);
						int charIndex = selection.getRanges().get(0).getStartSorted();
						selection.getCursor().getPrimary().setCharIndex(charIndex, true);
						selection.getCursor().clearOther();
					});
					TextSelection selectionText = new TextSelection(_mode, builder.toString());
					RegisterManager.getInstance().setRegister(registerRecognizer.getRegister(), selectionText);

					try (UndoScope us = buffer.createUndoScope()) {
						_window.getBufferController().getInnerSelections()
								.forEachReverse((Range range, String text) -> {
									buffer.removeCharsInRangeLogged(range);
								});
					}

					_window.switchToInputMode();
				}));
	}

	public VisualMode(BufferWindow window, SelectionMode mode) {
		super(window, mode, CURSOR_UNDERLINE);
		_mode = mode;

		addResponder(new CommandInputResponder(_window));
		addResponder(new SearchTextResponder(_window));
		NormalModeSwitchResponder normalModeSwitch = new NormalModeSwitchResponder(_window);
		addResponder(normalModeSwitch, () -> {
			normalModeSwitch.onRecognize();
		});
		BasicNavigationResponder navigationResponder = new BasicNavigationResponder(_window);
		addResponder(navigationResponder, () -> {
			navigationResponder.onRecognize();
		});
		addResponder((Event nextEvent) -> {
			return EventRecognition.matchesExact(nextEvent, "o");
		}, () -> {
			_window.getBufferController().switchToOther();
		});
		addDeleteResponder();
		addReplaceResponder();
		addCopyResponder();
	}

	@Override
	public void activate() {
		super.activate();
	}

	@Override
	public void deactivate() {
		super.deactivate();
	}

	@Override
	public Face getModelineFace() {
		return new Face(Color.YELLOW, Color.WHITE);
	}

	@Override
	public String getModeName() {
		switch (_mode) {
		case NORMAL_MODE:
			return "visual";
		case LINE_MODE:
			return "visual-line";
		case BLOCK_MODE:
			return "visual-block";
		default:
		}
		return "visual-undefined";
	}

}
