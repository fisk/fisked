package org.fisk.fisked.text;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

import org.fisk.fisked.ui.Point;

public class AttributedString {
    public static class AttributeSet {
        private TextColor _foregroundColour;
        private TextColor _backgroundColour;

        public AttributeSet(TextColor foregroundColour, TextColor backgroundColour) {
            _foregroundColour = foregroundColour;
            _backgroundColour = backgroundColour;
        }
    }

    public static class AttributedStringFragment {
        private String _string;
        private AttributeSet _attributes;

        public AttributedStringFragment(String string, AttributeSet attributes) {
            _string = string;
            _attributes = attributes;
        }

        public AttributeSet getAttributes() {
            return _attributes;
        }

        public String toString() {
            return _string;
        }
    }

    private List<AttributedStringFragment> _fragments = new ArrayList<>();
    private int _length = 0;

    public List<AttributedStringFragment> getFragments() {
        return _fragments;
    }

    public static AttributedString create(String string, TextColor foregroundColour, TextColor backgroundColour) {
        var str = new AttributedString();
        str.append(string, foregroundColour, backgroundColour);
        return str;
    }

    public void append(String string, TextColor foregroundColour, TextColor backgroundColour) {
        _fragments.add(new AttributedStringFragment(string, new AttributeSet(foregroundColour, backgroundColour)));
        _length += string.length();
    }

    public void append(AttributedString str) {
        _fragments.addAll(str._fragments);
        _length += str._length;
    }

    public void drawAt(Point point, TextGraphics graphics) {
        int currentX = 0;
        for (var fragment: _fragments) {
            graphics.setBackgroundColor(fragment.getAttributes()._backgroundColour);
            graphics.setForegroundColor(fragment.getAttributes()._foregroundColour);
            graphics.putString(point.getX() + currentX, point.getY(), fragment._string);
            currentX += fragment._string.length();
        }
    }

    public int length() {
        return _length;
    }
}
