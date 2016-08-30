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
package org.fisked.buffer.cursor;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.cursor.traverse.CursorStatus;
import org.fisked.buffer.cursor.traverse.IEdgeOrderer;
import org.fisked.buffer.cursor.traverse.IEdgeVisitor;
import org.fisked.buffer.cursor.traverse.ITraversable;
import org.fisked.buffer.cursor.traverse.IVertexOrderer;
import org.fisked.buffer.cursor.traverse.IVertexVisitor;
import org.fisked.text.TextLayout;
import org.fisked.util.models.Point;
import org.fisked.util.models.Range;
import org.fisked.util.models.selection.SelectionMode;

public class TwinCursor implements ITraversable {
	private Cursor _primaryCursor;
	private Cursor _otherCursor;

	private CursorStatus _cursorStatus = CursorStatus.ACTIVE;

	@Override
	public CursorStatus getCursorStatus() {
		return _cursorStatus;
	}

	@Override
	public void setCursorStatus(CursorStatus cursorStatus) {
		_cursorStatus = cursorStatus;
	}

	public TwinCursor(Cursor primaryCursor, Cursor otherCursor) {
		_primaryCursor = primaryCursor;
		_otherCursor = otherCursor;
	}

	public TwinCursor(Cursor cursor) {
		_primaryCursor = cursor;
		_otherCursor = null;
	}

	@Override
	public ITraversable clone() {
		return new TwinCursor((Cursor) _primaryCursor.clone(), (Cursor) _otherCursor.clone());
	}

	public void resetOther() {
		_otherCursor = (Cursor) _primaryCursor.clone();
	}

	public void clearOtherSorted() {
		Range range = getOtherRange();
		_primaryCursor.setCharIndex(range.getStart(), true);
		_otherCursor = null;
	}

	public void clearOther() {
		_otherCursor = null;
	}

	public Cursor getOther() {
		return _otherCursor;
	}

	public Cursor getPrimary() {
		return _primaryCursor;
	}

	public void setPrimary(Cursor primary) {
		_primaryCursor = primary;
	}

	public Range getOtherRange() {
		Cursor primary = _primaryCursor;
		if (_otherCursor == null) {
			return new Range(primary.getCharIndex(), 0);
		}
		Cursor other = _otherCursor;
		if (other == null) {
			return null;
		}
		int start = primary.getCharIndex();
		int end = other.getCharIndex();
		return new Range(start, end - start);
	}

	public Range getSortedOtherRange() {
		Cursor primary = _primaryCursor;
		if (_otherCursor == null) {
			return new Range(primary.getCharIndex(), 0);
		}
		Cursor other = _otherCursor;
		if (other == null) {
			return null;
		}
		int start = Math.min(primary.getCharIndex(), other.getCharIndex());
		int end = Math.max(primary.getCharIndex(), other.getCharIndex());
		return new Range(start, end - start);
	}

	public List<Range> getExpandedRanges(Buffer buffer, SelectionMode mode) {
		Range range = getSortedOtherRange();
		TextLayout layout = buffer.getTextLayout();
		List<Range> ranges = new ArrayList<>();
		switch (mode) {
		case LINE_MODE: {
			// Recalculate contiguous range to next case block
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
			ranges.add(range);
			break;
		}
		case BLOCK_MODE: {
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
			break;
		}
		default:
		}
		return ranges;
	}

	public void switchOther() {
		Cursor other = _otherCursor;
		_otherCursor = _primaryCursor;
		_primaryCursor = other;
	}

	@Override
	public boolean traverse(IVertexOrderer orderer, IVertexVisitor visitor) {
		return orderer.traverse(this, visitor);
	}

	@Override
	public boolean traverse(IEdgeOrderer orderer, IEdgeVisitor visitor) {
		return orderer.traverseEdge(this, visitor);
	}
}
