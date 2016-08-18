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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.buffer.cursor.TwinCursor;
import org.fisked.buffer.cursor.traverse.IEdge;
import org.fisked.buffer.cursor.traverse.IFilterEdgeVisitor;
import org.fisked.buffer.cursor.traverse.IFilterVertexVisitor;
import org.fisked.util.datastructure.IntervalTree;
import org.fisked.util.models.Range;
import org.fisked.util.models.selection.SelectionMode;

public class TextTransaction {
	private final Buffer _buffer;
	private List<TwinCursor> _cursors;
	private final boolean _logged;
	private final SelectionMode _mode;

	public TextTransaction(Buffer buffer, boolean logged, SelectionMode mode) {
		_logged = logged;
		_buffer = buffer;
		_mode = mode;
		addBufferCursorCollection();
	}

	private void addCursor(TwinCursor cursor) {
		_cursors.add(cursor);
	}

	private void addBufferCursorCollection() {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				addCursor(traversable);
				return true;
			}
		};
		_buffer.getCursorCollection().doFiltered(visitor);
	}

	public void executeDelete() {
		if (_logged) {
			try (UndoScope us = _buffer.createUndoScope()) {
				executeInternal();
			}
		} else {
			executeInternal();
		}
	}

	private void executeInternal() {
		IntervalTree<TwinCursor> removeIntervals = new IntervalTree<TwinCursor>();
		List<TwinCursor> deleteCursors = new ArrayList<>();
		NavigableMap<Integer, TwinCursor> cursorStarts = new TreeMap<>();
		for (TwinCursor cursor : _cursors) {
			List<Range> expandedRanges = cursor.getExpandedRanges(_buffer, _mode);
			for (Range expandedRange : expandedRanges) {
				if (removeIntervals.isIntersecting(expandedRange)) {
					removeIntervals.forEachIntersect(expandedRange,
							(Range intersectRange, TwinCursor intersectCursor) -> {
								deleteCursors.add(cursor);
								removeIntervals.deleteFirstIntersect(intersectRange);
								int min = Math.min(intersectRange.getStartSorted(), expandedRange.getStartSorted());
								int max = Math.max(intersectRange.getEndSorted(), expandedRange.getEndSorted());
								removeIntervals.add(new Range(min, max), intersectCursor);
								TwinCursor minCursor = null;
								if (intersectRange.getStartSorted() < expandedRange.getStartSorted()) {
									minCursor = intersectCursor;
								} else {
									minCursor = cursor;
								}
								cursorStarts.put(expandedRange.getStartSorted(), minCursor);
							});
				} else {
					removeIntervals.add(expandedRange, cursor);
					cursorStarts.put(expandedRange.getStartSorted(), cursor);
				}
			}
		}

		removeIntervals.forEachReverse((Range range, TwinCursor cursor) -> {
			if (_logged) {
				_buffer.removeCharsInRangeLogged(range);
			} else {
				_buffer.removeCharsInRange(range);
			}
			Entry<Integer, TwinCursor> entry = cursorStarts.higherEntry(range.getStart());
			while (entry != null) {
				int cursorStart = entry.getKey();
				TwinCursor currentCursor = entry.getValue();
				cursorStarts.remove(cursorStart, currentCursor);
				cursorStarts.put(cursorStart - range.getLength(), currentCursor);
				entry = cursorStarts.higherEntry(range.getEnd());
			}
		});

		for (Entry<Integer, TwinCursor> cursorEntry : cursorStarts.entrySet()) {
			int cursorStart = cursorEntry.getKey();
			TwinCursor currentCursor = cursorEntry.getValue();
			currentCursor.resetOther();
			currentCursor.getPrimary().setCharIndex(cursorStart, true);
		}

		for (TwinCursor deleteCursor : deleteCursors) {
			IFilterEdgeVisitor<TwinCursor> visitor = new IFilterEdgeVisitor<TwinCursor>() {
				@Override
				public boolean visitEdge(IEdge edge, TwinCursor traversable) {
					if (traversable.equals(deleteCursor)) {
						edge.delete();
						return false;
					}
					return true;
				}
			};
			_buffer.getCursorCollection().doFiltered(visitor);
		}
	}
}
