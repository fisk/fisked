package org.fisk.fisked.mode;

import org.fisk.fisked.ui.Window;

public class NormalMode extends Mode {
    public NormalMode() {
        super("NORMAL");
        setupBasicResponders();
    }

    private void setupBasicResponders() {
        _rootResponder.addEventResponder("q", () -> {
            System.exit(0);
        });
        _rootResponder.addEventResponder("i", () -> {
            var window = Window.getInstance();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("w", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().write();
        });
    }
}
