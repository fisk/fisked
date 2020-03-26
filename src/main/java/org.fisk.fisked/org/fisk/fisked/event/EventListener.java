package org.fisk.fisked.event;

public interface EventListener {
    public static enum Response {
        YES, NO, MAYBE
    }

    Response processEvent(KeyStrokeEvent event);
}
