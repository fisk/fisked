package org.fisk.fisked.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.googlecode.lanterna.TextColor;

import org.fisk.fisked.EventThread;
import org.fisk.fisked.event.RunnableEvent;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.Powerline;

public class ModeLineView extends View {
    private String _time;
    private String _mode = "NORMAL";
    private String _branch = "master";
    private String _line = "0:0";
    private String _name = "*scratch*";
    private TextColor _foregroundColour;

    private String getTime() {
        var format = DateFormat.getTimeInstance(2);
        return format.format(new Date());
    }

    private TextColor getModeColor() {
        switch (_mode) {
        case "NORMAL":
            return TextColor.ANSI.YELLOW;
        default:
            return null;
        }
    }

    public ModeLineView(Rect bounds) {
        super(bounds);
        setBackgroundColour(TextColor.Factory.fromString("#000000"));
        _foregroundColour = TextColor.ANSI.RED;
        _time = getTime();

        var timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var time = getTime();
                if (!time.equals(_time)) {
                    EventThread.getInstance().enqueue(new RunnableEvent(() -> {
                        _time = time;
                        ModeLineView.this.setNeedsRedraw();
                    }));
                }
            }
        }, 1000, 1000);
    }

    private AttributedString getLeftString() {
        var str = new AttributedString();
        TextColor modeColour = getModeColor();
        str.append(" " + _mode + " ", _backgroundColour, modeColour);
        str.append(Powerline.SYMBOL_FILLED_RIGHT_ARROW, modeColour, _backgroundColour);
        str.append(" " + _name + " ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_RIGHT_ARROW, _foregroundColour, _backgroundColour);
        str.append(" ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_LN, _foregroundColour, _backgroundColour);
        str.append(" " + _line + " ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_RIGHT_ARROW, _foregroundColour, _backgroundColour);
        return str;
    }

    private AttributedString getRightString() {
        var str = new AttributedString();
        str.append(Powerline.SYMBOL_LEFT_ARROW, _foregroundColour, _backgroundColour);
        str.append(" " + Powerline.SYMBOL_BRANCH + " ", _foregroundColour, _backgroundColour);
        str.append(_branch, _foregroundColour, _backgroundColour);
        str.append(" ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_LEFT_ARROW, _foregroundColour, _backgroundColour);
        str.append(" " + _time + " ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_LEFT_ARROW, _foregroundColour, _backgroundColour);
        return str;
    }

    private AttributedString getWhitespaces(int length) {
        var str = new StringBuffer();
        for (int i = 0; i < length; i++) {
            str.append(" ");
        }
        return AttributedString.create(str.toString(), TextColor.ANSI.DEFAULT, _backgroundColour);
    }

    private AttributedString getString() {
        var str = new AttributedString();
        var left = getLeftString();
        var right = getRightString();

        str.append(left);
        str.append(getWhitespaces(getBounds().getSize().getWidth() - left.length() - right.length()));
        str.append(right);

        return str;
    }

    @Override
    public void draw(Rect rect) {
        super.draw(rect);
        var terminalContext = TerminalContext.getInstance();
        var textGraphics = terminalContext.getGraphics();
        getString().drawAt(rect.getPoint(), textGraphics);
    }
}
