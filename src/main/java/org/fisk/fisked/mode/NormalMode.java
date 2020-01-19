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
        _rootResponder.addEventResponder("h", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goLeft();
        });
        _rootResponder.addEventResponder("l", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goRight();
        });
        _rootResponder.addEventResponder("j", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goDown();
        });
        _rootResponder.addEventResponder("k", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goUp();
        });
        _rootResponder.addEventResponder("<LEFT>", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goBack();
        });
        _rootResponder.addEventResponder("<RIGHT>", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goForward();
        });
        _rootResponder.addEventResponder("<DOWN>", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goDown();
        });
        _rootResponder.addEventResponder("<UP>", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goUp();
        });
    }
}
