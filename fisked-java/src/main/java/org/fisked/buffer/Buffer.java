package org.fisked.buffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.text.ITextDecorator;
import org.fisked.text.TextLayout;

public class Buffer {
	private AttributedString _string = null;
	private final StringBuilder _stringBuilder = new StringBuilder();
	private volatile BufferTextState _state = new BufferTextState("", null);
	private boolean _stringNeedsRebuilding = true;

	private FileContext _fileContext = null;
	private TextLayout _layout;
	private Cursor _cursor;
	private final Map<String, Object> _map = new HashMap<String, Object>();

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

	public ITextDecorator getSourceDecorator() {
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

	public Cursor getCursor() {
		return _cursor;
	}

	public void setCursor(Cursor cursor) {
		_cursor = cursor;
	}

	public void setTextLayout(TextLayout layout) {
		_layout = layout;
		_cursor = new Cursor(layout);
	}

	public TextLayout getTextLayout() {
		return _layout;
	}

	public void save() throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(_fileContext.getFile())) {
			out.print(_stringBuilder.toString());
		}
	}

	public void removeCharAtPointLogged() {
		if (_cursor.getCharIndex() == 0 || _stringBuilder.length() == 0)
			return;
		int index = _cursor.getCharIndex() - 1;
		removeCharsInRangeLogged(new Range(index, 1));
	}

	public void removeCharsInRange(Range selection) {
		_state = _state.deleteString(selection);
		_stringBuilder.delete(selection.getStart(), selection.getEnd());
		dirtyAttributedString();
		_cursor.setCharIndex(selection.getStart(), true);
	}

	public void removeCharsInRangeLogged(Range selection) {
		_undoLog.logDeleteString(selection);
		removeCharsInRange(selection);
	}

	public void appendCharAtPointLogged(char character) {
		appendStringAtPointLogged(Character.toString(character));
	}

	public int getPointIndex() {
		return _cursor.getCharIndex();
	}

	public void setPointIndex(int pointIndex) {
		if (pointIndex >= 0 && pointIndex <= _stringBuilder.length()) {
			_cursor.setCharIndex(pointIndex, true);
		}
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
		_cursor.setCharIndex(position + string.length(), true);
	}

	public void insertStringLogged(int position, String string) {
		_undoLog.logInsertString(position, string);
		insertString(position, string);
	}

	public void appendStringAtPointLogged(String string) {
		int index = _cursor.getCharIndex();
		insertStringLogged(index, string);
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

	public int getLength() {
		return _stringBuilder.length();
	}

	private final UndoLog _undoLog = new UndoLog(this);

	public UndoLog getUndoLog() {
		return _undoLog;
	}

	public void undo() {
		_undoLog.undo();
		dirtyAttributedString();
	}

	public void redo() {
		_undoLog.redo();
		dirtyAttributedString();
	}
}
