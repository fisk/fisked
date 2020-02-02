package org.fisk.fisked.ui;

import java.util.List;

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

public class ListView extends View {
    public static abstract class ListItem {
        public abstract void onClick();
        public abstract String displayString();
    }

    private List<? extends ListItem> _list;
    private String _title;
    private int _selection;
    private int _start;
    private StringBuilder _filter;
    protected ListEventResponder _responders = new ListEventResponder();

    public ListView(Rect bounds, List<? extends ListItem> list, String title) {
        super(bounds);
        _list = list;
        _title = title;
        _selection = 0;
        _responders.addEventResponder("<DOWN>", () -> {
            if (_selection >= list.size() - 1) {
                return;
            }
            ++_selection;
            ListView.this.setNeedsRedraw();
        });
        _responders.addEventResponder("<UP>", () -> {
            if (_selection <= 0) {
                return;
            }
            --_selection;
            ListView.this.setNeedsRedraw();
        });
        _responders.addEventResponder("<ESC>", () -> {
            ListView.this.getParent().setNeedsRedraw();
            Window.getInstance().hideList();
        });
        _responders.addEventResponder("<ENTER>", () -> {
            if (_selection >= _list.size()) {
                return;
            }
            var item = _list.get(_selection);
            ListView.this.getParent().setNeedsRedraw();
            item.onClick();
            Window.getInstance().hideList();
        });
        _responders.addEventResponder("<BACKSPACE>", () -> {
            if (_filter.length() > 0) {
                _filter.delete(_filter.length() - 1, _filter.length());
                ListView.this.setNeedsRedraw();
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
                _filter.append(_character);
                ListView.this.setNeedsRedraw();
            }
        });
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

        graphics.setBackgroundColor(TextColor.ANSI.GREEN);
        graphics.drawRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY()), new TerminalSize(rect.getSize().getWidth(), 1), ' ');
        var title = AttributedString.create(_title, TextColor.ANSI.BLACK, TextColor.ANSI.GREEN);
        title.drawAt(rect.getPoint(), graphics);

        graphics.setBackgroundColor(TextColor.ANSI.RED);
        graphics.drawRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY() + 1),
        new TerminalSize(rect.getSize().getWidth(), 1), ' ');
        var searchText = AttributedString.create("Filter: ", TextColor.ANSI.BLACK, TextColor.ANSI.RED);
        searchText.drawAt(Point.create(rect.getPoint().getX(), rect.getPoint().getY() + 1), graphics);

        int totalHeight = rect.getSize().getHeight();
        int listHeight = totalHeight - 2;

        if (_selection >= _start + listHeight) {
            _start = _selection - listHeight + 1;
        } else if (_selection < _start) {
            _start = _selection;
        }

        ListItem[] list = (ListItem[])_list.stream().filter((item) -> {return item.displayString().contains(_filter.toString());}).toArray();

        for (int i = _start; i < list.length && i - _start < listHeight; ++i) {
            var item = list[i];
            boolean selected = i == _selection;
            var str = AttributedString.create(item.displayString(),
                                              selected ? TextColor.ANSI.RED : TextColor.ANSI.GREEN,
                                              selected ? TextColor.ANSI.BLACK : _backgroundColour);
            str.drawAt(Point.create(0, i - _start + rect.getPoint().getY() + 2), graphics);
        }
    }
}
