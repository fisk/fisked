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
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.TwinCursor;
import org.fisked.buffer.cursor.traverse.CursorStatus;
import org.fisked.buffer.cursor.traverse.IEdge;
import org.fisked.buffer.cursor.traverse.IFilterEdgeVisitor;
import org.fisked.buffer.cursor.traverse.IFilterVertexVisitor;
import org.fisked.buffer.registers.RegisterManager;
import org.fisked.util.datastructure.IntervalTree;
import org.fisked.util.models.Range;
import org.fisked.util.models.selection.SelectionMode;
import org.fisked.util.models.selection.TextSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextTransaction {
	private final Buffer _buffer;
	private List<TwinCursor> _allCursors;
	private List<TwinCursor> _activeCursors;
	private List<TwinCursor> _inactiveCursors;
	private final boolean _logged;

	public TextTransaction(Buffer buffer, boolean logged) {
		_logged = logged;
		_buffer = buffer;
	}

	private void populateBufferCursorCollection(List<TwinCursor> list, CursorStatus status) {
		IFilterVertexVisitor<TwinCursor> visitor = new IFilterVertexVisitor<TwinCursor>() {
			@Override
			public boolean visit(TwinCursor traversable) {
				list.add(traversable);
				return true;
			}
		};
		_buffer.getCursorCollection().doFiltered(visitor, status);
	}

	private List<TwinCursor> getActiveCursors() {
		if (_activeCursors != null) {
			return _activeCursors;
		}
		_activeCursors = new ArrayList<>();
		populateBufferCursorCollection(_activeCursors, CursorStatus.ACTIVE);
		return _activeCursors;
	}

	private List<TwinCursor> getInactiveCursors() {
		if (_inactiveCursors != null) {
			return _inactiveCursors;
		}
		_inactiveCursors = new ArrayList<>();
		populateBufferCursorCollection(_inactiveCursors, CursorStatus.INACTIVE);
		return _inactiveCursors;
	}

	private List<TwinCursor> getAllCursors() {
		if (_allCursors != null) {
			return _allCursors;
		}
		_allCursors = new ArrayList<>();
		populateBufferCursorCollection(_allCursors, CursorStatus.ALL);
		return _allCursors;
	}

	private TreeMap<Integer, TwinCursor> getReverseActiveCursorTree() {
		TreeMap<Integer, TwinCursor> cursorStarts = new TreeMap<>(Collections.reverseOrder());

		for (TwinCursor cursor : getActiveCursors()) {
			cursorStarts.put(cursor.getSortedOtherRange().getStart(), cursor);
		}

		return cursorStarts;
	}

	private TreeMap<Integer, TwinCursor> getActiveCursorTree() {
		TreeMap<Integer, TwinCursor> cursorStarts = new TreeMap<>();

		for (TwinCursor cursor : getActiveCursors()) {
			cursorStarts.put(cursor.getSortedOtherRange().getStart(), cursor);
		}

		return cursorStarts;
	}

	private TreeMap<Integer, TwinCursor> getInactiveCursorTree() {
		TreeMap<Integer, TwinCursor> cursorStarts = new TreeMap<>();

		for (TwinCursor cursor : getInactiveCursors()) {
			cursorStarts.put(cursor.getSortedOtherRange().getStart(), cursor);
		}

		return cursorStarts;
	}

	private TreeMap<Integer, TwinCursor> getAllCursorTree() {
		TreeMap<Integer, TwinCursor> cursorStarts = new TreeMap<>();

		for (TwinCursor cursor : getAllCursors()) {
			cursorStarts.put(cursor.getSortedOtherRange().getStart(), cursor);
		}

		return cursorStarts;
	}

	private class CursorAdjuster {
		private final TreeMap<Integer, TwinCursor> _tree;
		private final int _strlen;
		private int _adjustment = 0;
		private Range _range = new Range(0, 0);
		private final int _buflen = _buffer.length();
		private TwinCursor _exclude = null;

		public CursorAdjuster(TreeMap<Integer, TwinCursor> tree, int strlen) {
			_strlen = strlen;
			_tree = tree;
		}

		private final Logger LOG = LoggerFactory.getLogger(CursorAdjuster.class);

		public void adjustToCursor(TwinCursor cursor) {
			LOG.debug("adjust cursor " + cursor + ": " + _range);
			Cursor primary = cursor.getPrimary();
			int end = primary.getCharIndex();
			int adjustedEnd = end + _adjustment;
			primary.setCharIndex(adjustedEnd, true);
			int start = _range.getStart();
			if (_exclude == null) {
				start = primary.getCharIndex();
			}
			_range = new Range(start, end - start);
			_exclude = cursor;
			LOG.debug("adjust cursor " + cursor + ": " + _range);
		}

		public void adjustToEnd() {
			LOG.debug("adjust end: " + _range);
			int end = _buflen;
			_range = new Range(_range.getStart(), end - _range.getStart());
			LOG.debug("adjust end: " + _range);
			flush();
			LOG.debug("adjust end: " + _range);
		}

		public void flush() {
			if (_adjustment != 0) {
				while (_range.getLength() > 0) {
					Entry<Integer, TwinCursor> entry = _tree.higherEntry(_range.getStart());
					if (entry == null) {
						LOG.debug("flush found null: " + _range);
						_range = new Range(_range.getEnd(), 0);
						LOG.debug("flush found null: " + _range);
					} else {
						int entryStart = entry.getKey();
						if (entryStart >= _range.getEnd() || _exclude == entry.getValue()) {
							LOG.debug("flush entry too high: " + _range + ", " + entryStart);
							_range = new Range(_range.getEnd(), 0);
							LOG.debug("flush entry too high: " + _range + ", " + entryStart);
						} else {
							TwinCursor twin = entry.getValue();
							Cursor primary = twin.getPrimary();
							int charIndex = primary.getCharIndex();
							primary.setCharIndex(charIndex + _adjustment, true);
							LOG.debug("flush found cursor: " + _range + ", " + charIndex + ", " + twin);
							_range = new Range(entry.getKey(), _range.getEnd() - entryStart);
							LOG.debug("flush found cursor: " + _range + ", " + charIndex + ", " + twin);
						}
					}
				}
			}
			_adjustment += _strlen;
		}
	}

	public void executeWrite(String string) {
		TreeMap<Integer, TwinCursor> activeCursorTree = getActiveCursorTree();
		Runnable internal = () -> {
			CursorAdjuster adjuster = new CursorAdjuster(getAllCursorTree(), string.length());
			activeCursorTree.forEach((start, cursor) -> {
				adjuster.adjustToCursor(cursor);
				if (_logged) {
					_buffer.appendStringAtPointLogged(cursor.getPrimary(), string);
				} else {
					_buffer.appendStringAtPoint(cursor.getPrimary(), string);
				}
				adjuster.flush();
			});
			adjuster.adjustToEnd();
		};
		if (_logged) {
			try (UndoScope us = _buffer.createUndoScope()) {
				internal.run();
			}
		} else {
			internal.run();
		}
	}

	public void executeDelete(RangeExpander expander) {
		if (_logged) {
			try (UndoScope us = _buffer.createUndoScope()) {
				executeDeleteInternal(expander);
			}
		} else {
			executeDeleteInternal(expander);
		}
	}

	public void executeDeleteSelection(SelectionMode mode) {
		RangeExpander expander = (TwinCursor cursor) -> {
			List<Range> ranges = cursor.getExpandedRanges(_buffer, mode);
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < ranges.size(); i++) {
				Range range = ranges.get(i);
				str.append(_buffer.getCharSequence().subSequence(range.getStart(), range.getEnd()));
				if (i + 1 < ranges.size()) {
					str.append("\n");
				}
			}
			RegisterManager.getInstance().setRegister(RegisterManager.UNNAMED_REGISTER,
					new TextSelection(mode, str.toString()));
			return ranges;
		};
		executeDelete(expander);
	}

	public void executeDeleteAtPoint(int forward, int backward) {
		RangeExpander expander = (TwinCursor cursor) -> {
			int charIndex = cursor.getPrimary().getCharIndex();
			Range range = new Range(charIndex - backward, forward + backward);
			List<Range> list = new ArrayList<>();
			list.add(range);
			return list;
		};
		executeDelete(expander);
	}

	private int getLineStart(Cursor cursor) {
		Buffer buffer = _buffer;
		String string = buffer.getCharSequence().toString();

		int newIndex = cursor.getCharIndex();
		if (newIndex != 0) {
			if (String.valueOf(string.charAt(newIndex - 1)).matches(".")) {
				newIndex--;
				while (newIndex >= 0 && String.valueOf(string.charAt(newIndex)).matches(".")) {
					newIndex--;
				}
				newIndex++;
			}
		}

		return newIndex;
	}

	private int getLineEnd(Cursor cursor) {
		Buffer buffer = _buffer;
		String string = buffer.getCharSequence().toString();

		int newIndex = cursor.getCharIndex();
		if (newIndex != string.length()) {
			if (String.valueOf(string.charAt(newIndex)).matches(".")) {
				newIndex++;
				while (newIndex < string.length() && String.valueOf(string.charAt(newIndex)).matches(".")) {
					newIndex++;
				}
			}
		}

		return newIndex;
	}

	public void executeDeleteLines() {
		RangeExpander expander = (TwinCursor twinCursor) -> {
			Cursor cursor = twinCursor.getPrimary();

			int start = getLineStart(cursor);
			int lineEnd = getLineEnd(cursor) + 1;
			int end = Math.min(lineEnd, _buffer.length());
			start -= lineEnd - end;
			start = Math.max(start, 0);
			Range lineRange = new Range(start, end - start);

			String string = _buffer.toString().substring(lineRange.getStart(), lineRange.getEnd());
			// TODO: Should have per-cursor registers really.
			RegisterManager.getInstance().setRegister(RegisterManager.UNNAMED_REGISTER,
					new TextSelection(SelectionMode.LINE_MODE, string));

			List<Range> list = new ArrayList<>();
			list.add(lineRange);
			return list;
		};
		executeDelete(expander);
	}

	public interface RangeExpander {
		List<Range> getRanges(TwinCursor cursor);
	}

	private final Logger LOG = LoggerFactory.getLogger(TextTransaction.class);

	private void executeDeleteInternal(RangeExpander expander) {
		IntervalTree<TwinCursor> removeIntervals = new IntervalTree<>();
		List<TwinCursor> deleteCursors = new ArrayList<>();
		TreeMap<Integer, TwinCursor> cursorStarts = new TreeMap<>();
		for (TwinCursor cursor : getActiveCursors()) {
			List<Range> expandedRanges = expander.getRanges(cursor);
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
			LOG.debug("Deleting range " + range);
			Entry<Integer, TwinCursor> entry = cursorStarts.higherEntry(range.getStart());
			LOG.debug("Searching for higher");
			while (entry != null) {
				int cursorStart = entry.getKey();
				TwinCursor currentCursor = entry.getValue();
				cursorStarts.remove(cursorStart, currentCursor);
				int newPosition = cursorStart - range.getLength();
				LOG.debug("Found higher " + cursorStart + " -> " + newPosition);
				if (cursorStarts.containsKey(newPosition)) {
					deleteCursors.add(currentCursor);
					LOG.debug("Delete " + cursorStart + " -> " + newPosition);
				} else {
					cursorStarts.put(newPosition, currentCursor);
					LOG.debug("Move " + cursorStart + " -> " + newPosition);
				}
				entry = cursorStarts.higherEntry(cursorStart);
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
			_buffer.getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
		}
	}
}
