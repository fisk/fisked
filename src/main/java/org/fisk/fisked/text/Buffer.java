package org.fisk.fisked.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fisk.fisked.ui.Cursor;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
    private int _position;
    private Cursor _cursor;
    private BufferContext _bufferContext;

    public Buffer() {
    }

    public Cursor getCursor() {
        return _cursor;
    }

    public Buffer(Path path, BufferContext bufferContext) {
        _path = path;
        _bufferContext = bufferContext;
        _cursor = new Cursor(bufferContext);
        try {
            _string.append(Files.readString(path));
        } catch (IOException e) {
        }
    }

    public void insert(String str) {
        _string.insert(_cursor.getPosition(), str);
        _bufferContext.getTextLayout().calculate();
        _cursor.goForward();
    }

    public void removeBefore() {
        if (_cursor.getPosition() == 0 || _string.length() == 0) {
            return;
        }
        _cursor.goBack();
        _string.deleteCharAt(_cursor.getPosition());
        _bufferContext.getTextLayout().calculate();
    }

    public void write() {
        try {
            Files.writeString(_path, _string.toString());
        } catch (IOException e) {
        }
    }

    public int getLength() {
        return _string.length();
    }

    public String getString() {
        return _string.toString();
    }
}
