package org.fisked.responder;

public class Event {
	private int _input;
	
	public Event(int input) {
		_input = input;
	}
	
	public boolean isCharacter() {
		return !Character.isISOControl(_input);
	}
	
	public int getCode() {
		return _input;
	}
	
	public boolean isControl() {
		return _input < 32;
	}
	
	public char getControlChar() {
		return (char)(((int)'a') + _input - 1);
	}
	
	public boolean isEscape() {
		return _input == 27;
	}
	
	public boolean isReturn() {
		return _input == 13;
	}
	
	public boolean isBackspace() {
		return _input == 127;
	}
	
	public boolean isDelete() {
		return _input == 330;
	}

	public char getCharacter() {
		return (char)_input;
	}

	public boolean isSpecialCode() {
		return Character.isISOControl(_input);
	}

}
