package org.fisked.responder;

import java.util.Iterator;

public class Event implements Iterable<Event> {
	private final int _input;
	private Event _next;
	
	public Event(int input) {
		_input = input;
	}
	
	public Event getNext() {
		return _next;
	}
	
	public void setNext(Event next) {
		_next = next;
	}
	
	public boolean hasNext() {
		return _next != null;
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
	
	private class EventIterator implements Iterator<Event> {
		Event _current = Event.this;

		@Override
		public boolean hasNext() {
			return _current != null;
		}

		@Override
		public Event next() {
			Event next = _current;
			_current = next.getNext();
			return next;
		}
	}

	@Override
	public Iterator<Event> iterator() {
		return new EventIterator();
	}

}
