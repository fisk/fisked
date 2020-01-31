package org.fisk.fisked.mode;

import org.fisk.fisked.ui.Window;

public class NormalMode extends Mode {
    private Window _window;

    public NormalMode(Window window) {
        super("NORMAL");
        _window = window;
        setupBasicResponders();
    }

    private void setupBasicResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        _rootResponder.addEventResponder("q", () -> { System.exit(0); });
        _rootResponder.addEventResponder("i", () -> { window.switchToMode(window.getInputMode()); });
        _rootResponder.addEventResponder("w", () -> { buffer.write(); });
        _rootResponder.addEventResponder("u", () -> { buffer.undo(); });
        _rootResponder.addEventResponder("<CTRL>-r", () -> {window.getBufferContext().getBuffer().redo(); });
        _rootResponder.addEventResponder("d i w", () -> {
            buffer.deleteInnerWord();
            buffer.getUndoLog().commit();
        });
        _rootResponder.addEventResponder("d w", () -> {
            buffer.deleteWord();
            buffer.getUndoLog().commit();
        });
        _rootResponder.addEventResponder("c i w", () -> {
            buffer.deleteInnerWord();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("c i w", () -> {
            buffer.deleteWord();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("<CTRL>-y", () -> { bufferContext.getBufferView().scrollUp(); });
        _rootResponder.addEventResponder("<CTRL>-e", () -> { bufferContext.getBufferView().scrollDown(); });
        _rootResponder.addEventResponder("a", () -> {
            window.switchToMode(window.getInputMode());
            buffer.getCursor().goRight();
        });
        _rootResponder.addEventResponder("A", () -> {
            window.switchToMode(window.getInputMode());
            cursor.goEndOfLine();
        });
        _rootResponder.addEventResponder("o", () -> {
            cursor.goEndOfLine();
            window.switchToMode(window.getInputMode());
            buffer.insert("\n");
        });
        _rootResponder.addEventResponder("$", () -> { cursor.goEndOfLine(); });
        _rootResponder.addEventResponder("^", () -> { cursor.goStartOfLine(); });
        _rootResponder.addEventResponder("h", () -> { cursor.goLeft(); });
        _rootResponder.addEventResponder("l", () -> { cursor.goRight(); });
        _rootResponder.addEventResponder("j", () -> { cursor.goDown(); });
        _rootResponder.addEventResponder("k", () -> { cursor.goUp(); });
        _rootResponder.addEventResponder("<LEFT>", () -> { cursor.goLeft(); });
        _rootResponder.addEventResponder("<RIGHT>", () -> { cursor.goRight(); });
        _rootResponder.addEventResponder("<DOWN>", () -> { cursor.goDown(); });
        _rootResponder.addEventResponder("<UP>", () -> { cursor.goUp(); });
    }

    @Override
    public void activate() {
        _window.getBufferContext().getBuffer().getUndoLog().commit();
    }
}
