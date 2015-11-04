package org.fisked.renderingengine.service.models;

public class Range {
	private final int _start;
	private final int _length;
	
	public Range(int start, int length) {
		_start = start;
		_length = length;
	}
	
	public int getStart() {
		return _start;
	}
	
	public int getLength() {
		return _length;
	}
	
	public int getEnd() {
		return _start + _length;
	}
}
