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

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferController;
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.registers.RegisterManager;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.mode.responder.SearchTextResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.selection.FatTextSelection;
import org.fisked.renderingengine.service.models.selection.Selection;
import org.fisked.renderingengine.service.models.selection.SelectionMode;
import org.fisked.renderingengine.service.models.selection.TextSelection;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.register.RegisterRecognizer;
import org.fisked.text.TextLayout;

public class VisualMode extends AbstractMode {
	private Cursor _activeCursor = _window.getBuffer().getCursor();
	private Cursor _inactiveCursor = Cursor.makeCursorFromCharIndex(_activeCursor.getCharIndex(),
			_window.getTextLayout());
	private SelectionMode _mode;

	private Selection getSelectionRange() {
		int cursor1 = _activeCursor.getCharIndex();
		int cursor2 = _inactiveCursor.getCharIndex();
		int minCursor = Math.min(cursor1, cursor2);
		int maxCursor = Math.max(cursor1, cursor2);
		int length = maxCursor - minCursor;
		return new Selection(minCursor, length, _mode);
	}

	private void setSelection() {
		BufferController controller = _window.getBufferController();
		controller.setSelection(getSelectionRange());
		_window.setNeedsFullRedraw();
	}

	private void clearSelection() {
		_window.getBufferController().setSelection(null);
	}

	private FatTextSelection getFatTextSelection() {
		Selection selection = getSelectionRange();
		Buffer buffer = _window.getBuffer();
		Range range = selection.getRange();
		List<Range> ranges = new ArrayList<>();
		switch (selection.getMode()) {
		case LINE_MODE: {
			// Recalculate contiguous range to next case block
			TextLayout layout = _window.getTextLayout();
			Point startPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getStart());
			Point endPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getEnd());

			int minY = Math.min(startPoint.getY(), endPoint.getY());
			int maxY = Math.max(startPoint.getY(), endPoint.getY());

			int minIndex;
			int maxIndex;

			try {
				minIndex = layout.getCharIndexForAbsolutePhysicalPoint(new Point(0, minY));
			} catch (Exception e) {
				minIndex = 0;
			}

			try {
				maxIndex = layout.getCharIndexForAbsolutePhysicalPoint(new Point(0, maxY + 1));
			} catch (Exception e) {
				maxIndex = buffer.length();
			}

			range = new Range(minIndex, maxIndex - minIndex);
		}
		case NORMAL_MODE: {
			// Contiguous text
			String str = _window.getBuffer().getCharSequence().subSequence(range.getStart(), range.getEnd()).toString();
			ranges.add(range);

			return new FatTextSelection(selection.getMode(), str, ranges);
		}
		case BLOCK_MODE: {
			TextLayout layout = _window.getTextLayout();
			StringBuilder stringBuilder = new StringBuilder();
			Point startPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getStart());
			Point endPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getEnd());

			int minY = Math.min(startPoint.getY(), endPoint.getY());
			int maxY = Math.max(startPoint.getY(), endPoint.getY());
			int minX = Math.min(startPoint.getX(), endPoint.getX());
			int maxX = Math.max(startPoint.getX(), endPoint.getX());

			for (int i = minY; i <= maxY; i++) {
				int minIndex;
				int maxIndex;
				int lineEnd;

				try {
					lineEnd = layout.getCharIndexForAbsolutePhysicalPoint(new Point(0, i + 1));
				} catch (Exception e) {
					lineEnd = buffer.length();
				}
				try {
					minIndex = layout.getCharIndexForAbsolutePhysicalPoint(new Point(minX, i));
				} catch (Exception e) {
					minIndex = lineEnd;
				}
				try {
					maxIndex = layout.getCharIndexForAbsolutePhysicalPoint(new Point(maxX, i));
				} catch (Exception e) {
					maxIndex = lineEnd;
				}

				ranges.add(new Range(minIndex, maxIndex - minIndex));
				stringBuilder.append(buffer.subSequence(minIndex, maxIndex));
			}
			return new FatTextSelection(SelectionMode.BLOCK_MODE, stringBuilder.toString(), ranges);
		}
		}
		return null;
	}

	private void addCopyResponder() {
		RegisterRecognizer registerRecognizer = new RegisterRecognizer();
		addResponder(EventRecognition.builder().optional(registerRecognizer).require(EventRecognition.recognizer("y"))
				.build(() -> {
					FatTextSelection selection = getFatTextSelection();
					RegisterManager.getInstance().setRegister(registerRecognizer.getRegister(), selection);
					clearSelection();
					_window.switchToNormalMode();
				}));
	}

	private void addDeleteResponder() {
		RegisterRecognizer registerRecognizer = new RegisterRecognizer();
		addResponder(EventRecognition.builder().optional(registerRecognizer).require(EventRecognition.recognizer("d"))
				.build(() -> {
					FatTextSelection selection = getFatTextSelection();
					RegisterManager.getInstance().setRegister(registerRecognizer.getRegister(), selection);
					List<Range> ranges = selection.getRanges();
					Buffer buffer = _window.getBuffer();
					buffer.pushUndoScope();
					int firstIndex = -1;
					for (int i = ranges.size() - 1; i >= 0; i--) {
						Range range = ranges.get(i);
						buffer.removeCharsInRangeLogged(range);
						firstIndex = range.getStart();
					}
					buffer.popUndoScope();
					clearSelection();
					_window.switchToNormalMode();
					_window.getBuffer().getCursor().setCharIndex(firstIndex, true);
					_window.setNeedsFullRedraw();
				}));
	}

	private void addReplaceResponder() {
		RegisterRecognizer registerRecognizer = new RegisterRecognizer();
		addResponder(EventRecognition.builder().optional(registerRecognizer).require(EventRecognition.recognizer("c"))
				.build(() -> {
					Selection selection = getSelectionRange();
					Range range = selection.getRange();
					if (selection.getMode() != SelectionMode.NORMAL_MODE)
						throw new RuntimeException("Not yet implemented");
					CharSequence string = _window.getBuffer().getCharSequence().subSequence(range.getStart(),
							range.getEnd());
					RegisterManager.getInstance().setRegister(registerRecognizer.getRegister(),
							new TextSelection(SelectionMode.NORMAL_MODE, string.toString()));
					_window.getBuffer().removeCharsInRangeLogged(range);
					clearSelection();
					_window.switchToInputMode();
					_window.setNeedsFullRedraw();
				}));
	}

	public VisualMode(BufferWindow window, SelectionMode mode) {
		super(window);
		_mode = mode;

		addResponder(new CommandInputResponder(_window));
		addResponder(new SearchTextResponder(_window));
		NormalModeSwitchResponder normalModeSwitch = new NormalModeSwitchResponder(_window);
		addResponder(normalModeSwitch, () -> {
			normalModeSwitch.onRecognize();
			clearSelection();
		});
		BasicNavigationResponder navigationResponder = new BasicNavigationResponder(_window);
		addResponder(navigationResponder, () -> {
			navigationResponder.onRecognize();
			setSelection();
		});
		addResponder((Event nextEvent) -> {
			return EventRecognition.matchesExact(nextEvent, "o");
		} , () -> {
			Cursor other = _inactiveCursor;
			_window.getBuffer().setCursor(other);
			_inactiveCursor = _activeCursor;
			_activeCursor = other;
		});
		addDeleteResponder();
		addReplaceResponder();
		addCopyResponder();
	}

	@Override
	public void activate() {
		changeCursor(CURSOR_UNDERLINE);
		setSelection();
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
		}
		return "visual-undefined";
	}

}
