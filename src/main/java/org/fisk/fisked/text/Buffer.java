package org.fisk.fisked.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
    private int _position;

    public Buffer() {
    }

    public Buffer(Path path) {
        _path = path;
        try {
            _string.append(Files.readString(path));
        } catch (IOException e) {
        }
    }

    public void insert(String str) {
        _string.insert(_position, str);
        _position += str.length();
    }

    public void write() {
        try {
            Files.writeString(_path, _string.toString());
        } catch (IOException e) {
        }
    }
}
