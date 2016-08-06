package org.fisked.buffer.basic.utilities;

import java.util.ArrayList;
import java.util.List;

import org.fisked.responder.Event;

public class EventBuilder {
	List<Event> _events = new ArrayList<Event>();

	public void add(Event event) {
		_events.add(event);
	}

	public Event build() {
		Event result = null;
		for (int i = _events.size() - 1; i >= 0; i--) {
			Event current = new Event(_events.get(i));
			current.setNext(result);
			result = current;
		}
		return result;
	}

	public void clear() {
		_events.clear();
	}
}
