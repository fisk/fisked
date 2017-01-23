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
package org.fisked.buffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.fisked.util.models.Range;

public class UndoLog {
	private final Stack<ReversableAction> _undoEntries = new Stack<>();
	private final Stack<ReversableAction> _redoEntries = new Stack<>();
	private final Buffer _buffer;

	public UndoLog(Buffer buffer) {
		_buffer = buffer;
	}

	private abstract class ActionEntry {
		public abstract void apply();
	}

	private class ReversableAction {
		private final ActionEntry _undoEntry;
		private final ActionEntry _redoEntry;

		public ReversableAction(ActionEntry undoEntry, ActionEntry redoEntry) {
			_undoEntry = undoEntry;
			_redoEntry = redoEntry;
		}

		public ActionEntry getUndoEntry() {
			return _undoEntry;
		}

		public ActionEntry getRedoEntry() {
			return _redoEntry;
		}
	}

	private class InsertEntry extends ActionEntry {
		private final int _position;
		private final String _string;

		public InsertEntry(int position, String string) {
			_position = position;
			_string = string;
		}

		@Override
		public void apply() {
			_buffer.insertString(_position, _string);
		}
	}

	private class DeleteEntry extends ActionEntry {
		private final Range _range;

		public DeleteEntry(Range range) {
			_range = range;
		}

		@Override
		public void apply() {
			_buffer.removeCharsInRange(_range);
		}
	}

	private class MergedActionEntry extends ActionEntry {
		private final List<ActionEntry> _entries = new ArrayList<>();

		public MergedActionEntry() {

		}

		void addEntry(ActionEntry entry) {
			_entries.add(entry);
		}

		@Override
		public void apply() {
			for (ActionEntry entry : _entries) {
				entry.apply();
			}
		}
	}

	public void logInsertString(int position, String string) {
		_redoEntries.clear();
		ActionEntry undoAction = new DeleteEntry(new Range(position, string.length()));
		ActionEntry redoAction = new InsertEntry(position, string);
		ReversableAction action = new ReversableAction(undoAction, redoAction);
		_undoEntries.push(action);
	}

	public void logDeleteString(Range range) {
		_redoEntries.clear();
		String string = _buffer.getCharSequence().subSequence(range.getStart(), range.getEnd()).toString();
		ActionEntry undoAction = new InsertEntry(range.getStart(), string);
		ActionEntry redoAction = new DeleteEntry(range);
		ReversableAction action = new ReversableAction(undoAction, redoAction);
		_undoEntries.push(action);
	}

	public void merge(int entries) {
		MergedActionEntry undoEntry = new MergedActionEntry();
		MergedActionEntry redoEntry = new MergedActionEntry();
		LinkedList<ActionEntry> redoEntries = new LinkedList<>();
		for (int i = 0; i < entries; i++) {
			ReversableAction entry = _undoEntries.pop();
			undoEntry.addEntry(entry.getUndoEntry());
			redoEntries.addFirst(entry.getRedoEntry());
		}
		for (ActionEntry entry : redoEntries) {
			redoEntry.addEntry(entry);
		}
		_undoEntries.push(new ReversableAction(undoEntry, redoEntry));
	}

	public void undo() {
		if (_undoEntries.isEmpty())
			return;
		ReversableAction action = _undoEntries.pop();
		action.getUndoEntry().apply();
		_redoEntries.push(action);
	}

	public void redo() {
		if (_redoEntries.isEmpty())
			return;
		ReversableAction action = _redoEntries.pop();
		action.getRedoEntry().apply();
		_undoEntries.push(action);
	}

}
