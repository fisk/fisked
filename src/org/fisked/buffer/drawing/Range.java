package org.fisked.buffer.drawing;

public class Range {
	private int _from;
	private int _to;
	
	public Range(int from, int to) {
		_from = from;
		_to = to;
	}
	
	public int getFrom() {
		return _from;
	}
	
	public int getTo() {
		return _to;
	}
}
