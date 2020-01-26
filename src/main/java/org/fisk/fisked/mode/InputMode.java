package org.fisk.fisked.mode;

import com.googlecode.lanterna.input.KeyType;

import org.fisk.fisked.event.EventListener;
import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.ui.Window;

public class InputMode extends Mode {
    public InputMode() {
        super("INPUT");
        setupBasicResponders();
    }

    private void setupBasicResponders() {
        _rootResponder.addEventResponder("<ESC>", () -> {
            var window = Window.getInstance();
            window.switchToMode(window.getNormalMode());
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
                var window = Window.getInstance();
                var bufferContext = window.getBufferContext();
                bufferContext.getBuffer().insert(Character.toString(_character));
                bufferContext.getBufferView().setNeedsRedraw();
                window.getModeLineView().setNeedsRedraw();
            }
        });
        _rootResponder.addEventResponder("<BACKSPACE>", () -> {
            var window = Window.getInstance();
            var bufferContext = window.getBufferContext();
            bufferContext.getBuffer().removeBefore();
            bufferContext.getBufferView().setNeedsRedraw();
            window.getModeLineView().setNeedsRedraw();
        });
        _rootResponder.addEventResponder("<ENTER>", () -> {
            var window = Window.getInstance();
            var bufferContext = window.getBufferContext();
            bufferContext.getBuffer().insert("\n");
            bufferContext.getBufferView().setNeedsRedraw();
            window.getModeLineView().setNeedsRedraw();
        });
    }

    @Override
    public void activate() {
        Window.getInstance().getBufferContext().getBuffer().getCursor().setAfter(true);
        Window.getInstance().getBufferContext().getBuffer().getCursor().goBack();;
    }
}
