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
        _rootResponder.addEventResponder("d i w", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().deleteInnerWord();;
        });
        _rootResponder.addEventResponder("c i w", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().deleteInnerWord();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("<CTRL>-y", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBufferView().scrollUp();
        });
        _rootResponder.addEventResponder("<CTRL>-e", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBufferView().scrollDown();
        });
        _rootResponder.addEventResponder("a", () -> {
            var window = Window.getInstance();
            window.switchToMode(window.getInputMode());
            window.getBufferContext().getBuffer().getCursor().goRight();
        });
        _rootResponder.addEventResponder("A", () -> {
            var window = Window.getInstance();
            window.switchToMode(window.getInputMode());
            window.getBufferContext().getBuffer().getCursor().goEndOfLine();
        });
        _rootResponder.addEventResponder("o", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goEndOfLine();
            window.switchToMode(window.getInputMode());
            window.getBufferContext().getBuffer().insert("\n");
        });
        _rootResponder.addEventResponder("$", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goEndOfLine();
        });
        _rootResponder.addEventResponder("^", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goStartOfLine();
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
            window.getBufferContext().getBuffer().getCursor().goLeft();
        });
        _rootResponder.addEventResponder("<RIGHT>", () -> {
            var window = Window.getInstance();
            window.getBufferContext().getBuffer().getCursor().goRight();
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
