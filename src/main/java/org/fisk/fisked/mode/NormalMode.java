package org.fisk.fisked.mode;

import com.googlecode.lanterna.input.KeyType;

import org.fisk.fisked.EventThread;
import org.fisk.fisked.event.EventListener;
import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.TextEventResponder;

public class NormalMode extends Mode {
    public NormalMode() {
        super("NORMAL");
        setupBasicResponders();
    }

    private void setupBasicResponders() {
        _rootResponder.addEventResponder(new TextEventResponder("q", () -> {
            System.exit(0);
        }));
    }
}
