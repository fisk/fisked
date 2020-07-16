package org.fisk.fisked.ui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.fisk.fisked.EventThread;
import org.fisk.fisked.event.RunnableEvent;
import org.fisk.fisked.fileindex.ProjectPaths;
import org.fisk.fisked.terminal.TerminalContext;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.Powerline;

import com.googlecode.lanterna.TextColor;

public class ModeLineView extends View {
    private String _time;
    private TextColor _foregroundColour;

    private String getTime() {
        var format = DateFormat.getTimeInstance(2);
        return format.format(new Date());
    }

    private String getMode() {
        return Window.getInstance().getCurrentMode().getName();
    }
    
    private String getName() {
        var window = Window.getInstance();
        var buffer = window.getBufferContext().getBuffer();
        var path = buffer.getPath();
        if (path == null) {
            return "*scratch*";
        }
        var root = ProjectPaths.getProjectRootPath();
        if (root != null) {
            return root.relativize(path).toString();
        } else {
            return buffer.getPath().toString();
        }
    }
    
    private String getBranch() {
        try {
            var repo = new FileRepositoryBuilder()
                    .setGitDir(ProjectPaths.getProjectRootPath().resolve(".git").toFile())
                    .build();
            return repo.getBranch();
        } catch (IOException e) {
            return "";
        }
    }

    private String getLine() {
        var window = Window.getInstance();
        var buffer = window.getBufferContext().getBuffer();
        var cursor = buffer.getCursor();
        var textLayout = window.getBufferContext().getTextLayout();
        var line = cursor.getYAbsolute();
        var position = cursor.getPosition();
        var index = textLayout.getLogicalLineAt(position).getIndex(position);
        return "" + position + ": " + line + ", " + index;
    }

    private TextColor getModeColor() {
        switch (getMode()) {
        case "NORMAL":
            return TextColor.ANSI.YELLOW;
        case "INPUT":
            return TextColor.ANSI.RED;
        case "VISUAL":
            return TextColor.ANSI.GREEN;
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
        str.append(" " + getMode() + " ", _backgroundColour, modeColour);
        str.append(Powerline.SYMBOL_FILLED_RIGHT_ARROW, modeColour, _backgroundColour);
        str.append(" " + getName() + " ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_RIGHT_ARROW, _foregroundColour, _backgroundColour);
        str.append(" ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_LN, _foregroundColour, _backgroundColour);
        str.append(" " + getLine() + " ", _foregroundColour, _backgroundColour);
        str.append(Powerline.SYMBOL_RIGHT_ARROW, _foregroundColour, _backgroundColour);
        return str;
    }

    private AttributedString getRightString() {
        var str = new AttributedString();
        str.append(Powerline.SYMBOL_LEFT_ARROW, _foregroundColour, _backgroundColour);
        str.append(" " + Powerline.SYMBOL_BRANCH + " ", _foregroundColour, _backgroundColour);
        str.append(getBranch(), _foregroundColour, _backgroundColour);
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
