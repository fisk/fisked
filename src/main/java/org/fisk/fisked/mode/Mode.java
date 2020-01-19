package org.fisk.fisked.mode;

import java.io.IOException;

import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.ListEventResponder;
import org.fisk.fisked.ui.Window;

public class Mode implements EventResponder {
    protected ListEventResponder _rootResponder = new ListEventResponder();
    private String _name;

    public Mode(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    @Override
    public Response processEvent(KeyStrokeEvent event) {
        return _rootResponder.processEvent(event);
    }

    @Override
    public void respond() {
        _rootResponder.respond();
    }

    private String esc() {
        return "\u001B";
    }

    private String csi() {
        return esc() + "[";
    }

    protected static enum CursorMode {
        Insert,
        Replace,
        Normal
    }

    protected void sendEscapeSequence(String str) {
        System.out.print(str);
        // When in tmux, we must escape the escape sequence...
        System.out.print(esc() + "Ptmux;" + esc() + str + esc() + "\\");
        System.out.flush();
    }

    protected void activateCursorMode(CursorMode mode) {
        // VTE compatible
        sendEscapeSequence(csi() + 5 + " q");
        // KDE4, iTerm2
        sendEscapeSequence(esc() + "]50;CursorShape=" + 0 + "\u0007");
        sendEscapeSequence(esc() + "]50;BlinkingCursorEnabled=1\u0007");
    }

    public void activate() {
        Window.getInstance().getBufferContext().getBuffer().getCursor().setAfter(false);
        // This stuff isn't quite working.
        //activateCursorMode(CursorMode.Normal);
    }
}
