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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.CursorCollection;
import org.fisked.buffer.cursor.traverse.CursorReverseDFSOrderer;
import org.fisked.buffer.cursor.traverse.IFilterVisitor;
import org.fisked.text.IBufferDecorator;
import org.fisked.text.TextLayout;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Range;

public class Buffer implements CharSequence {
	private AttributedString _string = null;
	private final StringBuilder _stringBuilder = new StringBuilder();
	private volatile BufferTextState _state = new BufferTextState("", null);
	private boolean _stringNeedsRebuilding = true;

	private FileContext _fileContext = null;
	private TextLayout _layout;
	private CursorCollection _cursorCollection;
	private final Map<String, Object> _map = new HashMap<String, Object>();

	private final Stack<Integer> _undoMergeScope = new Stack<>();

	public class UndoScope implements AutoCloseable {
		public UndoScope() {
			pushUndoScope();
		}

		@Override
		public void close() {
			popUndoScope();
		}
	}

	public UndoScope createUndoScope() {
		return new UndoScope();
	}

	public CursorCollection getCursorCollection() {
		return _cursorCollection;
	}

	public void pushUndoScope() {
		_undoMergeScope.push(0);
	}

	public void popUndoScope() {
		int merges = _undoMergeScope.pop();
		_undoLog.merge(merges);
	}

	private void incUndoScope() {
		if (_undoMergeScope.isEmpty()) {
			return;
		}
		int current = _undoMergeScope.pop();
		_undoMergeScope.push(current + 1);
	}

	public BufferTextState getBufferTextState() {
		return _state;
	}

	public AttributedString getAttributedString() {
		if (_stringNeedsRebuilding || _string == null) {
			_stringNeedsRebuilding = false;
			_string = new AttributedString(_stringBuilder.toString());
		}
		if (_string.length() != _stringBuilder.length())
			throw new IllegalStateException("Attributed string should have been reconstructed");
		return _string;
	}

	public void dirtyAttributedString() {
		_stringNeedsRebuilding = true;
		_layout.setNeedsLayout();
	}

	public FileContext getFileContext() {
		return _fileContext;
	}

	public IBufferDecorator getSourceDecorator() {
		if (_fileContext != null) {
			return _fileContext.getSourceDecorator();
		} else {
			return (state, callback) -> callback.call(new AttributedString(state.toString()));
		}
	}

	public Object getProperty(String key) {
		return _map.get(key);
	}

	public void setProperty(String key, Object value) {
		_map.put(key, value);
	}

	public Buffer() {
	}

	public Buffer(File file) throws IOException {
		_fileContext = new FileContext(file);
		file.createNewFile();
		String fileContent = IOUtils.toString(file.toURI(), Charset.forName("UTF-8"));
		_stringBuilder.append(fileContent);
		_state = new BufferTextState(fileContent, null);
	}

	public Buffer(String string) {
		_stringBuilder.append(string);
		_state = new BufferTextState(string, null);
	}

	public void setTextLayout(TextLayout layout) {
		_layout = layout;
		_cursorCollection = new CursorCollection(layout);
		_cursorCollection.init();
	}

	public TextLayout getTextLayout() {
		return _layout;
	}

	public void save() throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(_fileContext.getFile())) {
			out.print(_stringBuilder.toString());
		}
	}

	private void removeCharAtPointLogged(Cursor cursor) {
		if (cursor.getCharIndex() == 0 || _stringBuilder.length() == 0)
			return;
		int index = cursor.getCharIndex() - 1;
		removeCharsInRangeLogged(new Range(index, 1));
	}

	public void removeCharAtPointLogged() {
		IFilterVisitor<Cursor> visitor = new IFilterVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				removeCharAtPointLogged(cursor);
				return true;
			}
		};
		_cursorCollection.doFiltered(visitor, new CursorReverseDFSOrderer());
	}

	public void removeCharsInRange(Range selection) {
		_state = _state.deleteString(selection);
		_stringBuilder.delete(selection.getStart(), selection.getEnd());
		dirtyAttributedString();
	}

	public void removeCharsInRangeLogged(Range selection) {
		_undoLog.logDeleteString(selection);
		incUndoScope();
		removeCharsInRange(selection);
	}

	public void appendCharAtPointLogged(char character) {
		appendStringAtPointLogged(Character.toString(character));
	}

	@Override
	public String toString() {
		return _stringBuilder.toString();
	}

	public CharSequence getCharSequence() {
		return _stringBuilder;
	}

	public void insertString(int position, String string) {
		_state = _state.insertString(position, string);
		_stringBuilder.insert(position, string);
		dirtyAttributedString();
	}

	public void insertStringLogged(int position, String string) {
		_undoLog.logInsertString(position, string);
		incUndoScope();
		insertString(position, string);
	}

	private void appendStringAtPointLogged(Cursor cursor, String string) {
		int index = cursor.getCharIndex();
		insertStringLogged(index, string);
	}

	public void appendStringAtPointLogged(String string) {
		IFilterVisitor<Cursor> visitor = new IFilterVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				appendStringAtPointLogged(cursor, string);
				return true;
			}
		};
		_cursorCollection.doFiltered(visitor, new CursorReverseDFSOrderer());
	}

	public String getFileName() {
		if (_fileContext != null) {
			return _fileContext.getFile().getName();
		} else {
			return "*scratch*";
		}
	}

	public File getFile() {
		if (_fileContext != null) {
			return _fileContext.getFile();
		} else {
			return null;
		}
	}

	private final UndoLog _undoLog = new UndoLog(this);

	public void undo() {
		_undoLog.undo();
		dirtyAttributedString();
	}

	public void redo() {
		_undoLog.redo();
		dirtyAttributedString();
	}

	@Override
	public int length() {
		return _stringBuilder.length();
	}

	@Override
	public char charAt(int index) {
		return _stringBuilder.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return _stringBuilder.subSequence(start, end);
	}
}
