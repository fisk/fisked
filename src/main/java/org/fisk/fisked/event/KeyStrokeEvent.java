package org.fisk.fisked.event;

import com.googlecode.lanterna.input.KeyStroke;

public class KeyStrokeEvent extends Event {
    private KeyStrokeEvent _previous;
    private KeyStroke _keyStroke;

    public KeyStrokeEvent(KeyStroke keyStroke) {
        _keyStroke = keyStroke;
    }

    public KeyStroke getKeyStroke() {
        return _keyStroke;
    }

    public void setPrevious(KeyStrokeEvent previous) {
        _previous = previous;
    }

    public KeyStrokeEvent getPrevious() {
        return _previous;
    }
}
