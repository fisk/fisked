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
	private boolean _stringNeedsRebuilding = true;

	private FileContext _fileContext = null;
	private TextLayout _layout;
	private Cursor _cursor;
	private final Map<String, Object> _map = new HashMap<String, Object>();

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
		getSourceDecorator().setNeedsRedraw();
		_layout.setNeedsLayout();
	}

	public FileContext getFileContext() {
		return _fileContext;
	}

	public ITextDecorator getSourceDecorator() {
		if (_fileContext != null) {
			return _fileContext.getSourceDecorator();
		} else {
			return new ITextDecorator() {
				@Override
				public void setNeedsRedraw() {
				}

				@Override
				public AttributedString decorate(AttributedString string) {
					return string;
				}
			};
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
		_stringBuilder.append(IOUtils.toString(file.toURI(), Charset.forName("UTF-8")));
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

	public void removeCharAtPoint() {
		if (_cursor.getCharIndex() > 0 && _stringBuilder.length() > 0) {
			_stringBuilder.deleteCharAt(_cursor.getCharIndex() - 1);
			dirtyAttributedString();
			_cursor.setCharIndex(_cursor.getCharIndex() - 1, true);
		}
	}

	public void removeCharsInRange(Range selection) {
		_stringBuilder.delete(selection.getStart(), selection.getEnd());
		dirtyAttributedString();
		_cursor.setCharIndex(selection.getStart(), true);
	}

	public void appendCharAtPoint(char character) {
		if (_cursor.getCharIndex() == _stringBuilder.length()) {
			_stringBuilder.append(character);
			dirtyAttributedString();
			_cursor.setCharIndex(_cursor.getCharIndex() + 1, true);
		} else {
			_stringBuilder.insert(_cursor.getCharIndex(), character);
			dirtyAttributedString();
			_cursor.setCharIndex(_cursor.getCharIndex() + 1, true);
		}
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

	public void appendStringAtPoint(String string) {
		if (_cursor.getCharIndex() == _stringBuilder.length()) {
			_stringBuilder.append(string);
			dirtyAttributedString();
			_cursor.setCharIndex(_cursor.getCharIndex() + string.length(), true);
		} else {
			_stringBuilder.insert(_cursor.getCharIndex(), string);
			dirtyAttributedString();
			_cursor.setCharIndex(_cursor.getCharIndex() + string.length(), true);
		}
	}

	public String getFileName() {
		if (_fileContext != null) {
			return _fileContext.getFile().getName();
		} else {
			return "*scratch*";
		}
	}

	public File getFile() {
		return _fileContext.getFile();
	}

	public int getLength() {
		return _stringBuilder.length();
	}
}
