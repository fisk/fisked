package org.fisk.fisked.event;

import java.util.ArrayList;
import java.util.List;

public class ListEventResponder implements EventResponder {
    private EventResponder _responder;
    private List<EventResponder> _responders = new ArrayList<EventResponder>();

    public void addEventResponder(EventResponder responder) {
        _responders.add(responder);
    }

    public void addEventResponder(String pattern, Runnable runnable) {
        _responders.add(new TextEventResponder(pattern, runnable));
    }

    public void removeEventResponder(EventResponder responder) {
        _responders.remove(responder);
    }

    @Override
    public Response processEvent(KeyStrokeEvent event) {
        boolean maybe = false;
        EventResponder yes = null;
        for (var responder : _responders) {
            var response = responder.processEvent(event);
            if (response == EventListener.Response.MAYBE) {
                maybe = true;
            }
            if (response == EventListener.Response.YES) {
                yes = responder;
            }
        }
        if (maybe) {
            return EventListener.Response.MAYBE;
        } else if (yes != null) {
            _responder = yes;
            return EventListener.Response.YES;
        } else {
            return EventListener.Response.NO;
        }
    }

    @Override
    public void respond() {
        _responder.respond();
        _responder = null;
    }
}
