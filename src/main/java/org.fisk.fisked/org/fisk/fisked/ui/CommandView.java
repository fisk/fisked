package org.fisk.fisked.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;

import org.fisk.fisked.event.EventListener;
import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokeEvent;
import org.fisk.fisked.event.ListEventResponder;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.AttributedString;

public class CommandView extends View {
    private String _message = null;
    private StringBuilder _command = null;
    private ListEventResponder _responders = new ListEventResponder();

    public CommandView(Rect bounds) {
        super(bounds);
        _responders.addEventResponder("<ESC>", () -> {
            deactivate();
        });
        _responders.addEventResponder("<ENTER>", () -> {
            runCommand(_command.toString());
            deactivate();
        });
        _responders.addEventResponder("<BACKSPACE>", () -> {
            if (_command.length() > 0) {
                _command.delete(_command.length() - 1, _command.length());
                CommandView.this.setNeedsRedraw();
            }
        });
        _responders.addEventResponder(new EventResponder() {
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
                _command.append(_character);
                CommandView.this.setNeedsRedraw();
            }
        });
    }

    private void runCommand(String command) {
        switch (command) {
        case "q":
            System.exit(0);
        case "w":
            Window.getInstance().getBufferContext().getBuffer().write();
            break;
        }
    }

    @Override
    public EventListener.Response processEvent(KeyStrokeEvent event) {
        return _responders.processEvent(event);
    }

    @Override
    public void respond() {
        _responders.respond();
    }

    @Override
    public void draw(Rect rect) {
        super.draw(rect);
        var terminalContext = TerminalContext.getInstance();
        var graphics = terminalContext.getGraphics();

        graphics.setBackgroundColor(TextColor.ANSI.BLACK);
        graphics.drawRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY()),
        new TerminalSize(rect.getSize().getWidth(), 1), ' ');

        if (_message != null) {
            var message = AttributedString.create(_message, TextColor.ANSI.DEFAULT, _backgroundColour);
            message.drawAt(rect.getPoint(), graphics);
        } else if (_command != null) {
            var message = AttributedString.create(":" + _command, TextColor.ANSI.DEFAULT, _backgroundColour);
            message.drawAt(rect.getPoint(), graphics);
        }
    }

    public void setMessage(String message) {
        _message = message;
        setNeedsRedraw();
    }

    public void activate() {
        _message = null;
        _command = new StringBuilder();
        var window = Window.getInstance();
        var rootView = window.getRootView();
        rootView.setFirstResponder(this);
        rootView.setNeedsRedraw();
    }

    private void deactivate() {
        _command = null;
        var window = Window.getInstance();
        var rootView = window.getRootView();
        rootView.setFirstResponder(window.getBufferContext().getBufferView());
        rootView.setNeedsRedraw();
    }
}
