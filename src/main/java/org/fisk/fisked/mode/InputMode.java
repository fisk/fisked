package org.fisk.fisked.mode;

import com.googlecode.lanterna.input.KeyType;

import org.fisk.fisked.event.EventListener;
import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.ui.Window;

public class InputMode extends Mode {
    public InputMode(Window window) {
        super("INPUT", window);
        setupBasicResponders();
    }

    private void setupBasicResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        _rootResponder.addEventResponder("<ESC>", () -> {
            window.switchToMode(window.getNormalMode());
            buffer.getCursor().goLeft();
        });
        _rootResponder.addEventResponder(new EventResponder() {
            private char _character;

            @Override
            public Response processEvent(KeyStrokeEvent event) {
                if (event.getKeyStroke().getKeyType() == KeyType.Character) {
                    _character = event.getKeyStroke().getCharacter();
                    return EventListener.Response.YES;
                }
                return EventListener.Response.NO;
            }

            @Override
            public void respond() {
                buffer.insert(Character.toString(_character));
                bufferContext.getBufferView().setNeedsRedraw();
                window.getModeLineView().setNeedsRedraw();
            }
        });
        _rootResponder.addEventResponder("<BACKSPACE>", () -> { buffer.removeBefore(); });
        _rootResponder.addEventResponder("<ENTER>", () -> { buffer.insert("\n"); });
        _rootResponder.addEventResponder("<LEFT>", () -> { cursor.goLeft(); });
        _rootResponder.addEventResponder("<RIGHT>", () -> { cursor.goRight(); });
        _rootResponder.addEventResponder("<DOWN>", () -> { cursor.goDown(); });
        _rootResponder.addEventResponder("<UP>", () -> { cursor.goUp(); });
    }

    @Override
    public void activate() {
        _window.getBufferContext().getBuffer().getCursor().setAfter(true);
    }
}
