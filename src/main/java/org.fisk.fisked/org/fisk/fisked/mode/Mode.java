package org.fisk.fisked.mode;

import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.FindResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.ListEventResponder;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.TextLayout.Glyph;
import org.fisk.fisked.ui.Drawable;
import org.fisk.fisked.ui.Rect;
import org.fisk.fisked.ui.Window;

public class Mode implements EventResponder, Drawable {
    protected Window _window;
    protected ListEventResponder _rootResponder = new ListEventResponder();
    private String _name;

    public Mode(String name, Window window) {
        _name = name;
        _window = window;
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

    protected void setupNavigationResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        _rootResponder.addEventResponder("<CTRL>-y", () -> { bufferContext.getBufferView().scrollUp(); });
        _rootResponder.addEventResponder("<CTRL>-e", () -> { bufferContext.getBufferView().scrollDown(); });
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
        _rootResponder.addEventResponder("g g", () -> { cursor.setPosition(0); });
        _rootResponder.addEventResponder("G", () -> { cursor.setPosition(buffer.getLength() - 1); });
        //_rootResponder.addEventResponder(new FindResponder(bufferContext, "f", true));
        //_rootResponder.addEventResponder(new FindResponder(bufferContext, "F", false));
    }

    public void activate() {
    }

    public void deactivate() {
        _window.getBufferContext().getBuffer().clearCursors();
    }

    @Override
    public void draw(Rect rect) {
    }
    
    public AttributedString decorate(Glyph glyph, AttributedString character) {
        return character;
    }
}
