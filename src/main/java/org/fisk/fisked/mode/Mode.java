package org.fisk.fisked.mode;

import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.ListEventResponder;

public class Mode implements EventResponder {
    protected ListEventResponder _rootResponder = new ListEventResponder();
    private String _name;

    public Mode(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    @Override
    public Response processEvent(KeyStrokeEvent event) {
        return _rootResponder.processEvent(event);
    }

    @Override
    public void respond() {
        _rootResponder.respond();
    }
}
