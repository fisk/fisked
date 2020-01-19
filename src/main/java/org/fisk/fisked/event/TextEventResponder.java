package org.fisk.fisked.event;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.input.KeyType;

public class TextEventResponder implements EventResponder {
    private String[] _keyStrokes;
    private Runnable _action;

    public TextEventResponder(String string, Runnable action) {
        _keyStrokes = string.split(" ");
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

        if (events.size() > _keyStrokes.length) {
            return EventListener.Response.NO;
        }

        int processed = 0;

        for (var e: events) {
            var keyStroke = e.getKeyStroke();
            String str = _keyStrokes[processed++];
            switch (keyStroke.getKeyType()) {
            case Character:
                if (keyStroke.getCharacter() != str.charAt(0)) {
                    return EventListener.Response.NO;
                }
                break;
            case Escape:
                if (!str.equals("<ESC>")) {
                    return EventListener.Response.NO;
                }
                break;
            case Backspace:
                if (!str.equals("<BACKSPACE>")) {
                    return EventListener.Response.NO;
                }
                break;
            case Enter:
                if (!str.equals("<ENTER>")) {
                    return EventListener.Response.NO;
                }
                break;
            case ArrowUp:
                if (!str.equals("<UP>")) {
                    return EventListener.Response.NO;
                }
                break;
            case ArrowDown:
                if (!str.equals("<DOWN>")) {
                    return EventListener.Response.NO;
                }
                break;
            case ArrowLeft:
                if (!str.equals("<LEFT>")) {
                    return EventListener.Response.NO;
                }
                break;
            case ArrowRight:
                if (!str.equals("<RIGHT>")) {
                    return EventListener.Response.NO;
                }
                break;
            default:
                return EventListener.Response.NO;
            }
        }

        if (processed == _keyStrokes.length) {
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
