package org.fisk.fisked.ui;

public class Range {
    private int _start;
    private int _end;

    private Range(int start, int end) {
        _start = start;
        _end = end;
    }

    public static Range create(int start, int end) {
        return new Range(start, end);
    }

    public int getStart() {
        return _start;
    }

    public int getEnd() {
        return _end;
    }
}
