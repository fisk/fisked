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
package org.fisked.buffer.controller;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.TwinCursor;
import org.fisked.buffer.cursor.traverse.CursorStatus;
import org.fisked.buffer.cursor.traverse.CursorVertexNonPrimaryOrderer;
import org.fisked.buffer.cursor.traverse.CursorVertexPrimaryOrderer;
import org.fisked.buffer.cursor.traverse.IFilterVertexVisitor;
import org.fisked.text.TextLayout;
import org.fisked.ui.buffer.BufferView;
import org.fisked.util.datastructure.IntervalTree;
import org.fisked.util.models.Point;
import org.fisked.util.models.Range;
import org.fisked.util.models.Rectangle;
import org.fisked.util.models.Size;
import org.fisked.util.models.selection.SelectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferController {
	private final static Logger LOG = LoggerFactory.getLogger(BufferController.class);
	private Buffer _buffer;
	private final BufferView _bufferView;
	private TextLayout _layout;
	private final Size _size;
	private SelectionMode _mode = SelectionMode.NORMAL_MODE;

	public SelectionMode getSelectionMode() {
		return _mode;
	}

	public void setSelectionMode(SelectionMode mode) {
		_mode = mode;
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				if (mode == SelectionMode.INVALID_MODE) {
					traversable.clearOther();
				} else {
					traversable.resetOther();
				}
				return true;
			}
		};
		_buffer.getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public BufferController(BufferView bufferView, Size size) {
		_bufferView = bufferView;
		_size = size;
		setBuffer(new Buffer());
	}

	public TextLayout getTextLayout() {
		return _layout;
	}

	public Buffer getBuffer() {
		return _buffer;
	}

	public BufferView getBufferView() {
		return _bufferView;
	}

	public Point getPrimaryLogicalPoint() {
		return _buffer.getCursorCollection().getPrimaryCursor().getRelativePoint();
	}

	public Point getPrimaryAbsolutePoint() {
		return _buffer.getCursorCollection().getPrimaryCursor().getAbsolutePoint();
	}

	public int getPrimaryCharIndex() {
		return _buffer.getCursorCollection().getPrimaryCursor().getCharIndex();
	}

	public void setBuffer(Buffer buffer) {
		_buffer = buffer;
		_layout = new TextLayout(_buffer, _size);
		_buffer.setTextLayout(_layout);
	}

	public interface IStringDecorator {
		void draw(Point point, String string, int offset);
	}

	public void drawBuffer(Rectangle drawingRect, IStringDecorator decorator) {
		_layout.getLogicalString((int offset, String line, Point relativePoint, boolean physicalLine) -> {
			decorator.draw(relativePoint, line, offset);
		});
	}

	private FatTextSelection getFatTextSelection(Range range, SelectionMode mode, TwinCursor cursor) {
		List<Range> ranges = new ArrayList<>();
		switch (mode) {
		case LINE_MODE: {
			// Recalculate contiguous range to next case block
			Point startPoint = _layout.getAbsolutePhysicalPointForCharIndex(range.getStart());
			Point endPoint = _layout.getAbsolutePhysicalPointForCharIndex(range.getEnd());

			int minY = Math.min(startPoint.getY(), endPoint.getY());
			int maxY = Math.max(startPoint.getY(), endPoint.getY());

			int minIndex;
			int maxIndex;

			try {
				minIndex = _layout.getCharIndexForAbsolutePhysicalPoint(new Point(0, minY));
			} catch (Exception e) {
				minIndex = 0;
			}

			try {
				maxIndex = _layout.getCharIndexForAbsolutePhysicalPoint(new Point(0, maxY + 1));
			} catch (Exception e) {
				maxIndex = _buffer.length();
			}

			range = new Range(minIndex, maxIndex - minIndex);
		}
		case NORMAL_MODE: {
			// Contiguous text
			String str = _buffer.getCharSequence().subSequence(range.getStart(), range.getEnd()).toString();
			ranges.add(range);

			return new FatTextSelection(mode, str, ranges, cursor);
		}
		case BLOCK_MODE: {
			StringBuilder stringBuilder = new StringBuilder();
			Point startPoint = _layout.getAbsolutePhysicalPointForCharIndex(range.getStart());
			Point endPoint = _layout.getAbsolutePhysicalPointForCharIndex(range.getEnd());

			int minY = Math.min(startPoint.getY(), endPoint.getY());
			int maxY = Math.max(startPoint.getY(), endPoint.getY());
			int minX = Math.min(startPoint.getX(), endPoint.getX());
			int maxX = Math.max(startPoint.getX(), endPoint.getX());

			for (int i = minY; i <= maxY; i++) {
				int minIndex;
				int maxIndex;
				int lineEnd;

				try {
					lineEnd = _layout.getCharIndexForAbsolutePhysicalPoint(new Point(0, i + 1));
				} catch (Exception e) {
					lineEnd = _buffer.length();
				}
				try {
					minIndex = _layout.getCharIndexForAbsolutePhysicalPoint(new Point(minX, i));
				} catch (Exception e) {
					minIndex = lineEnd;
				}
				try {
					maxIndex = _layout.getCharIndexForAbsolutePhysicalPoint(new Point(maxX, i));
				} catch (Exception e) {
					maxIndex = lineEnd;
				}

				ranges.add(new Range(minIndex, maxIndex - minIndex));
				stringBuilder.append(_buffer.subSequence(minIndex, maxIndex));
			}
			return new FatTextSelection(SelectionMode.BLOCK_MODE, stringBuilder.toString(), ranges, cursor);
		}
		default:
		}
		return null;
	}

	public String getMergedSelectedText() {
		StringBuilder builder = new StringBuilder();
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				Range range = traversable.getOtherRange();
				FatTextSelection selection = getFatTextSelection(range, _mode, traversable);
				builder.append(selection.getText());
				return true;
			}
		};
		_buffer.getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
		return builder.toString();
	}

	public void setMergedSelectionText(String text) {
		try (UndoScope us = _buffer.createUndoScope()) {
			TextTransaction transaction = _buffer.makeTextTransaction(true);
			transaction.executeDeleteSelection(_mode);
			transaction.executeWrite(text);
		}
	}

	public List<FatTextSelection> getFatTextSelections() {
		if (_mode == SelectionMode.INVALID_MODE)
			return new ArrayList<>();
		List<FatTextSelection> result = new ArrayList<>();
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				Range range = traversable.getSortedOtherRange();
				FatTextSelection selection = getFatTextSelection(range, _mode, traversable);
				result.add(selection);
				return true;
			}
		};
		_buffer.getCursorCollection().doFilteredReverse(visitor, CursorStatus.ACTIVE);
		return result;
	}

	public IntervalTree<String> getInnerSelections() {
		if (_mode == SelectionMode.INVALID_MODE)
			return new IntervalTree<>();
		IntervalTree<String> result = new IntervalTree<>();
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				Range range = traversable.getSortedOtherRange();
				FatTextSelection selection = getFatTextSelection(range, _mode, traversable);
				for (Range innerRange : selection.getRanges()) {
					String str = _buffer.subSequence(innerRange.getStart(), innerRange.getEnd()).toString();
					result.add(innerRange, str);
				}
				return true;
			}
		};
		_buffer.getCursorCollection().doFilteredReverse(visitor, CursorStatus.ACTIVE);
		return result;
	}

	public void collapseCursors(int charIndex) {
		_buffer.getCursorCollection().collapseCursors(charIndex);
	}

	public void switchToOther() {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				traversable.switchOther();
				return true;
			}
		};
		_buffer.getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public interface DoCursorClosure {
		void doit(Cursor cursor);
	}

	public interface DoClosure<T> {
		void doit(T cursor);
	}

	public void collapseCursors() {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor cursor) {
				Range range = cursor.getSortedOtherRange();
				cursor.getPrimary().setCharIndex(range.getStart(), true);
				cursor.clearOther();
				return true;
			}
		};
		getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void doNonPrimaryCursors(DoClosure<TwinCursor> closure, CursorStatus status) {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor cursor) {
				closure.doit(cursor);
				return true;
			}
		};
		getBuffer().getCursorCollection().doFiltered(visitor, new CursorVertexNonPrimaryOrderer(status));
	}

	public void doPrimaryCursor(DoClosure<TwinCursor> closure) {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor cursor) {
				closure.doit(cursor);
				return true;
			}
		};
		getBuffer().getCursorCollection().doFiltered(visitor, new CursorVertexPrimaryOrderer(CursorStatus.ACTIVE));
	}

	public void addCursorAtPoint(boolean activated) {
		getBuffer().getCursorCollection().addCursorAt(getPrimaryCharIndex(), activated);
	}

	public void activateExtraCursors() {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor cursor) {
				cursor.setCursorStatus(CursorStatus.ACTIVE);
				return true;
			}
		};
		getBuffer().getCursorCollection().doFiltered(visitor, new CursorVertexNonPrimaryOrderer(CursorStatus.ALL));
	}

	public void deactivateExtraCursors() {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor cursor) {
				cursor.setCursorStatus(CursorStatus.INACTIVE);
				return true;
			}
		};
		getBuffer().getCursorCollection().doFiltered(visitor, new CursorVertexNonPrimaryOrderer(CursorStatus.ALL));
	}
}
