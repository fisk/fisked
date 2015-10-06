package org.fisked.buffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.text.TextLayout;

public class Buffer {
	private File _file = null;
	private StringBuilder _buffer = new StringBuilder();
	private TextLayout _layout;
	private Cursor _cursor;
	
	public Buffer() {}

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
	
	public Buffer(File file) throws IOException {
		_file = file;
		file.createNewFile();
		_buffer.append(IOUtils.toString(_file.toURI(), Charset.forName("UTF-8")));
	}
	
	public void save() throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(_file)) {
			out.print(_buffer.toString());
		}
	}
	
	public void removeCharAtPoint() {
		if (_cursor.getCharIndex() > 0 && _buffer.length() > 0) {
			_buffer.deleteCharAt(_cursor.getCharIndex() - 1);
			_layout.setNeedsLayout();
			_cursor.setCharIndex(_cursor.getCharIndex() - 1, true);
		}
	}

	public void removeCharsInRange(Range selection) {
		_buffer.delete(selection.getStart(), selection.getEnd());
		_layout.setNeedsLayout();
		_cursor.setCharIndex(selection.getStart(), true);
	}
	
	public void appendCharAtPoint(char character) {
		if (_cursor.getCharIndex() == _buffer.length()) {
			_buffer.append(character);
			_layout.setNeedsLayout();
			_cursor.setCharIndex(_cursor.getCharIndex() + 1, true);
		} else {
			_buffer.insert(_cursor.getCharIndex(), character);
			_layout.setNeedsLayout();
			_cursor.setCharIndex(_cursor.getCharIndex() + 1, true);
		}
	}
	
	public int getPointIndex() {
		return _cursor.getCharIndex();
	}
	
	public void setPointIndex(int pointIndex) {
		if (pointIndex >= 0 && pointIndex <= _buffer.length()) {
			_cursor.setCharIndex(pointIndex, true);
		}
	}
	
	public String toString() {
		return _buffer.toString();
	}
	
	public StringBuilder getStringBuilder() {
		return _buffer;
	}

	public void appendStringAtPoint(String string) {
		if (_cursor.getCharIndex() == _buffer.length()) {
			_buffer.append(string);
			_layout.setNeedsLayout();
			_cursor.setCharIndex(_cursor.getCharIndex() + string.length(), true);
		} else {
			_buffer.insert(_cursor.getCharIndex(), string);
			_layout.setNeedsLayout();
			_cursor.setCharIndex(_cursor.getCharIndex() + string.length(), true);
		}
		
	}

	public String getFileName() {
		if (_file != null) {
			return _file.getName();
		} else {
			return "*scratch*";
		}
	}
}
