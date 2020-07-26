package org.fisk.fisked.ui;

import java.nio.file.Paths;

import org.fisk.fisked.event.EventResponder;
import org.fisk.fisked.event.KeyStrokes;
import org.fisk.fisked.event.ListEventResponder;
import org.fisk.fisked.event.Response;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.AttributedString;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;

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
            runCommand(_command.toString().split(" "));
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
            public Response processEvent(KeyStrokes events) {
                if (events.remaining() != 0) {
                    return Response.NO;
                }
                var event = events.current();
                if (event.getKeyType() == KeyType.Character) {
                    _character = event.getCharacter();
                    return Response.YES;
                }
                return Response.NO;
            }

            @Override
            public void respond() {
                _command.append(_character);
                CommandView.this.setNeedsRedraw();
            }
        });
    }

    private void runCommand(String[] args) {
        if (args.length == 0) {
            return;
        }
        String command = args[0];
        switch (command) {
        case "q":
            System.exit(0);
        case "e":
            open(args);
            break;
        case "w":
            Window.getInstance().getBufferContext().getBuffer().write();
            break;
        }
    }
    
    private void open(String[] args) {
        if (args.length != 2) {
            _message = "Wrong number of parameters";
            return;
        }
        var path = Paths.get(args[1]).toAbsolutePath();
        if (!path.toFile().exists()) {
            try {
                if (path.toFile().createNewFile()) {
                    Window.getInstance().setBufferPath(path);
                    return;
                }
            } catch (Exception e) {
            }
            _message = "File does not exist";
        } else {
            Window.getInstance().setBufferPath(path);
        }
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        return _responders.processEvent(events);
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
