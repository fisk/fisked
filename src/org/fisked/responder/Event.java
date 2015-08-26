package org.fisked.responder;

import jcurses.system.InputChar;

public class Event {
	private InputChar _input;
	
	public Event(InputChar input) {
		_input = input;
	}
	
	public boolean isCharacter() {
		return !_input.isSpecialCode() || isEscape();
	}
	
	public int getCode() {
		return _input.getCode();
	}
	
	public boolean isEscape() {
		return _input.getCode() == 27;
	}

	public char getCharacter() {
		return _input.getCharacter();
	}

	public boolean isSpecialCode() {
		return !isCharacter();
	}

}
