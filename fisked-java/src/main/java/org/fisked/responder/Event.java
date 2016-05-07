/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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

	public String getString() {
		StringBuilder builder = new StringBuilder();
		for (Event current = this; current != null; current = current._next) {
			if (!current.isCharacter()) {
				return null;
			}
			builder.append(current.getCharacter());
		}
		return builder.toString();
	}

	public Event subevent(int length) {
		Event cloneRoot = new Event(_input);
		Event prevClone = cloneRoot;
		Event current = _next;
		int i = 1;
		while (current != null && i < length) {
			Event currentClone = new Event(current._input);
			prevClone._next = currentClone;
			prevClone = currentClone;
			current = current._next;
			i++;
		}
		return cloneRoot;
	}

	public Event get(int index) {
		Event current = this;
		for (int i = 0; i < index; i++) {
			current = current._next;
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	public void setNext(Event next) {
		_next = next;
	}

	public int length() {
		int length = 0;
		for (Event current = this; current != null; current = current._next) {
			length++;
		}
		return length;
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
		return (char) ('a' + _input - 1);
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
		return (char) _input;
	}

	public boolean matchesCharacter(boolean control, boolean meta, char character) {
		if (!control && !meta && isCharacter() && getCharacter() == character)
			return true;
		if (control && !meta && isControlChar() && getControlChar() == character)
			return true;
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
