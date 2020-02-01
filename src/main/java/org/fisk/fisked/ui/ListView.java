package org.fisk.fisked.ui;

import java.util.List;

import com.googlecode.lanterna.TextColor;

import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.AttributedString;

public class ListView extends View {
    public abstract class ListItem {
        public abstract void onClick();
        public abstract String displayString();
    }

    private List<ListItem> _list;

    public ListView(Rect bounds, List<ListItem> list) {
        super(bounds);
    }

    @Override
    public void draw(Rect rect) {
        super.draw(rect);
        var terminalContext = TerminalContext.getInstance();
        var graphics = terminalContext.getGraphics();
        int height = rect.getSize().getHeight();
        for (int i = 0; i < _list.size() && i < height; ++i) {
            var item = _list.get(i);
            var str = AttributedString.create(item.displayString(), TextColor.ANSI.BLUE, _backgroundColour);
            str.drawAt(Point.create(0, i + rect.getPoint().getY()), graphics);
        }
    }
}
