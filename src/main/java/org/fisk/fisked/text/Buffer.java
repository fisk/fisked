package org.fisk.fisked.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
    private int _position;
    private BufferContext _bufferContext;

    public Buffer() {
    }

    public Buffer(Path path, BufferContext bufferContext) {
        _path = path;
        _bufferContext = bufferContext;
        try {
            _string.append(Files.readString(path));
        } catch (IOException e) {
        }
    }

    public void insert(String str) {
        _string.insert(_position, str);
        _position += str.length();
        _bufferContext.getTextLayout().calculate();
    }

    public void removeBefore() {
        if (_position == 0 || _string.length() == 0) {
            return;
        }

        _string.deleteCharAt(--_position);
        _bufferContext.getTextLayout().calculate();
    }

    public void write() {
        try {
            Files.writeString(_path, _string.toString());
        } catch (IOException e) {
        }
    }

    public String getString() {
        return _string.toString();
    }

    public int getPosition() {
        return _position;
    }
}
