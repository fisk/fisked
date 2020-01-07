package org.fisk.fisked;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.fisk.fisked.event.Event;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.ListEventResponder;
import org.fisk.fisked.event.RunnableEvent;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

public class EventThread extends Thread {
    private static Logger _log = LogFactory.createLog();

    private KeyStrokeEvent _previous;
    private final ListEventResponder _responder;
    private final ArrayBlockingQueue<Event> _events = new ArrayBlockingQueue<>(1024, true);
    private static volatile EventThread _instance;
    private final List<Runnable> _onEventRunnables = new ArrayList<>();

    public static EventThread getInstance() {
        var instance = _instance;
        if (instance == null) {
            synchronized (EventThread.class) {
                instance = _instance;
                if (instance == null) {
                    instance = new EventThread();
                    _instance = instance;
                }
            }
        }
        return instance;
    }

    public EventThread() {
        _responder = new ListEventResponder();
    }

    @Override
    public void run() {
        while (true) {
            Event event = null;
            while (true) {
                try {
                    event = _events.poll(1, TimeUnit.SECONDS);
                    _log.info("Poked event");
                    if (event != null) {
                        break;
                    }
                } catch (InterruptedException e) {}
            }
            if (event instanceof KeyStrokeEvent) {
                _log.info("Received key stroke event");
                var keyEvent = (KeyStrokeEvent) event;
                keyEvent.setPrevious(_previous);
                switch (_responder.processEvent(keyEvent)) {
                case MAYBE:
                    _previous = keyEvent;
                    break;
                case YES:
                    _responder.respond();
                case NO:
                    _previous = null;
                    break;
                }
            } else if (event instanceof RunnableEvent) {
                _log.info("Received runnable event");
                var runnableEvent = (RunnableEvent) event;
                runnableEvent.execute();
            }
            _log.info("Run post-event hooks");
            for (Runnable runnable: _onEventRunnables) {
                runnable.run();
            }
            _log.info("Ran post-event hooks");
        }
    }

    public void enqueue(Event event) {
        while (true) {
            try {
                if (_events.offer(event, 1, TimeUnit.SECONDS)) {
                    _log.info("Sent event");
                    return;
                }
            } catch (InterruptedException e) {}
        }
    }

    public ListEventResponder getResponder() {
        return _responder;
    }

    public void addOnEvent(Runnable runnable) {
        _onEventRunnables.add(runnable);
    }
}
