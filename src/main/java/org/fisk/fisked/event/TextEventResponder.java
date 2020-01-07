package org.fisk.fisked.event;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.input.KeyType;

public class TextEventResponder implements EventResponder {
    private String _string;
    private Runnable _action;

    public TextEventResponder(String string, Runnable action) {
        _string = string;
        _action = action;
    }

    private void constructEvents(KeyStrokeEvent event, List<KeyStrokeEvent> events) {
        if (event == null) {
            return;
        }
        constructEvents(event.getPrevious(), events);
        events.add(event);
    }

    @Override
    public EventListener.Response processEvent(KeyStrokeEvent event) {
        List<KeyStrokeEvent> events = new ArrayList<>();
        constructEvents(event, events);
        int processed = 0;

        for (var e: events) {
            if (e.getKeyStroke().getKeyType() != KeyType.Character) {
                return EventListener.Response.NO;
            }
            if (!e.getKeyStroke().getCharacter().equals(_string.charAt(processed++))) {
                return EventListener.Response.NO;
            }
        }

        if (processed == _string.length()) {
            return EventListener.Response.YES;
        } else {
            return EventListener.Response.MAYBE;
        }
    }

	@Override
	public void respond() {
      _action.run();
	}
}
