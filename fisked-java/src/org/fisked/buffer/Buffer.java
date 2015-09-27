package org.fisked.buffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Buffer {
	private File _file = null;
	private StringBuilder _buffer = new StringBuilder();
	private int _pointIndex = 0;
	
	public Buffer() {}
	
	public Buffer(File file) throws IOException {
		_file = file;
		file.createNewFile();
	}
	
	public void save() throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(_file)) {
			out.print(_buffer.toString());
		}
	}
	
	public void removeCharAtPoint() {
		if (_pointIndex > 0 && _buffer.length() > 0) {
			_buffer.deleteCharAt(_pointIndex - 1);
			_pointIndex--;
		}
	}
	
	public void appendCharAtPoint(char character) {
		_buffer.append(character);
		_pointIndex++;
	}
	
	public int getPointIndex() {
		return _pointIndex;
	}
	
	public String toString() {
		return _buffer.toString();
	}
	
	public StringBuilder getStringBuilder() {
		return _buffer;
	}

	public void appendStringAtPoint(String string) {
		_buffer.append(string);
		_pointIndex += string.length();
	}

	public void setPointIndex(int index) {
		_pointIndex = index;
	}
}
