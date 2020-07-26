package org.fisk.fisked.event;

public interface EventListener {
    Response processEvent(KeyStrokes events);
}
