package org.fisked.buffer;

public class Buffer {
	private StringBuilder _buffer;
	
	private int _pointIndex = 0;
	
	public Buffer() {
		_buffer = new StringBuilder();
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
}
