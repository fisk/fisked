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

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.selection.Selection;
import org.fisked.text.IBufferDecorator;
import org.fisked.text.TextLayout;
import org.fisked.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferView extends View {
	private final static Logger LOG = LoggerFactory.getLogger(BufferView.class);
	BufferController _controller;

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

		Selection selection = _controller.getSelection();

		if (selection == null) {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attributedSubstring = attributedString.substring(offset, offset + str.length());
				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attributedSubstring);
			});
		} else {
			Range range = selection.getRange();

			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attributedSubstring = attributedString.substring(offset, offset + str.length());

				switch (selection.getMode()) {
				case NORMAL_MODE: {
					int relativeSelectionStart = Math.max(range.getStart() - offset, 0);
					int relativeSelectionEnd = Math.min(range.getEnd() - offset, str.length());

					if (relativeSelectionEnd - relativeSelectionStart > 0) {
						attributedSubstring.setForegroundColor(selectionForegroundColor, relativeSelectionStart,
								relativeSelectionEnd);
						attributedSubstring.setBackgroundColor(selectionBackgroundColor, relativeSelectionStart,
								relativeSelectionEnd);
					}
					break;
				}
				case LINE_MODE: {
					TextLayout layout = _controller.getTextLayout();
					Point startPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getStart());
					Point endPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getEnd());

					int minY = Math.min(startPoint.getY(), endPoint.getY());
					int maxY = Math.max(startPoint.getY(), endPoint.getY());

					LOG.debug("Line mode selection minY: " + minY + ", maxY: " + maxY);

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
						maxIndex = str.length();
					}

					LOG.debug("Line mode selection minIndex: " + minIndex + ", maxIndex: " + maxIndex);

					int relativeSelectionStart = Math.max(minIndex - offset, 0);
					int relativeSelectionEnd = Math.min(maxIndex - offset, str.length());

					if (relativeSelectionEnd - relativeSelectionStart > 0) {
						attributedSubstring.setForegroundColor(selectionForegroundColor, 0, str.length());
						attributedSubstring.setBackgroundColor(selectionBackgroundColor, 0, str.length());
					}
					break;
				}
				case BLOCK_MODE: {
					TextLayout layout = _controller.getTextLayout();
					Point startPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getStart());
					Point endPoint = layout.getAbsolutePhysicalPointForCharIndex(range.getEnd());

					int minY = Math.min(startPoint.getY(), endPoint.getY());
					int maxY = Math.max(startPoint.getY(), endPoint.getY());
					int minX = Math.min(startPoint.getX(), endPoint.getX());
					int maxX = Math.max(startPoint.getX(), endPoint.getX());

					int physicalLine = layout.getAbsolutePhysicalPointForCharIndex(offset).getY();

					if (physicalLine >= minY && physicalLine <= maxY) {
						int minIndex;
						int maxIndex;
						try {
							minIndex = layout.getCharIndexForAbsolutePhysicalPoint(new Point(minX, physicalLine));
						} catch (Exception e) {
							minIndex = offset + str.length();
						}
						try {
							maxIndex = layout.getCharIndexForAbsolutePhysicalPoint(new Point(maxX, physicalLine));
						} catch (Exception e) {
							maxIndex = offset + str.length();
						}

						int relativeSelectionStart = Math.max(minIndex - offset, 0);
						int relativeSelectionEnd = Math.min(maxIndex - offset, str.length());

						if (relativeSelectionEnd - relativeSelectionStart > 0) {
							attributedSubstring.setForegroundColor(selectionForegroundColor, relativeSelectionStart,
									relativeSelectionEnd);
							attributedSubstring.setBackgroundColor(selectionBackgroundColor, relativeSelectionStart,
									relativeSelectionEnd);
						}
					}
					break;
				}
				}

				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attributedSubstring);
			});
		}
	}
}
