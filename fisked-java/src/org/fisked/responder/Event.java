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
	
	public boolean isControlChar() {
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
	
	public boolean matchesCharacter(boolean control, boolean meta, char character) {
		if (!control && !meta && isCharacter() && getCharacter() == character) return true;
		if (control && !meta && isControlChar() && getControlChar() == character) return true;
		return false;
	}

	public boolean isSpecialCode() {
		return Character.isISOControl(_input);
	}
	
	public boolean isCharacter(char character) {
		return matchesCharacter(false, false, character);
	}

	public boolean isControlChar(char character) {
		return matchesCharacter(true, false, character);
	}

}
