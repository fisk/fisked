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
package org.fisked.buffer;

import org.fisked.buffer.cursor.FatTextSelection;
import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.text.IBufferDecorator;
import org.fisked.theme.ThemeManager;
import org.fisked.util.datastructure.IntervalTree;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Color;
import org.fisked.util.models.Point;
import org.fisked.util.models.Range;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferView extends View {
	private final static Logger LOG = LoggerFactory.getLogger(BufferView.class);
	private BufferController _controller;

	public BufferView(Rectangle frame) {
		super(frame);
	}

	public void setBufferController(BufferController controller) {
		_controller = controller;
	}

	@Override
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);

		Buffer buffer = _controller.getBuffer();
		IBufferDecorator decorator = buffer.getSourceDecorator();

		AttributedString attributedString = buffer.getBufferTextState().decorate(decorator).copy();

		Color selectionBackgroundColor = ThemeManager.getThemeManager().getCurrentTheme().getSelectionBackgroundColor();
		Color selectionForegroundColor = ThemeManager.getThemeManager().getCurrentTheme().getSelectionForegroundColor();

		IntervalTree<FatTextSelection> selections = _controller.getFatTextSelections();

		if (selections.isEmpty()) {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attributedSubstring = attributedString.substring(offset, offset + str.length());
				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attributedSubstring);
			});
		} else {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attributedSubstring = attributedString.substring(offset, offset + str.length());

				Range queryRange = new Range(offset, str.length());
				LOG.debug("Query range: " + queryRange);

				selections.forEachIntersect(queryRange, (Range cursorRange, FatTextSelection selection) -> {
					LOG.debug("Intersection: " + cursorRange);
					for (Range range : selection.getRanges()) {
						int relativeSelectionStart = Math.max(range.getStart() - offset, 0);
						int relativeSelectionEnd = Math.min(range.getEnd() - offset, str.length());

						if (relativeSelectionEnd - relativeSelectionStart > 0) {
							attributedSubstring.setForegroundColor(selectionForegroundColor, relativeSelectionStart,
									relativeSelectionEnd);
							attributedSubstring.setBackgroundColor(selectionBackgroundColor, relativeSelectionStart,
									relativeSelectionEnd);
						}
					}
				});

				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attributedSubstring);
			});
		}
	}
}
