package org.fisk.fisked.ui;

import org.fisk.fisked.text.Buffer;

public class BufferView extends View {
    private Buffer _buffer;

    public BufferView(Rect rect, Buffer buffer) {
        super(rect);
        _buffer = buffer;
    }
}
