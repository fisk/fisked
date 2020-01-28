package org.fisk.fisked.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.fisk.fisked.ui.Cursor;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
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

    public String getCharacter(int position) {
        if (position < 0 || _string.length() == 0 || position >= _string.length()) {
            return "";
        }
        return _string.substring(position, position + 1);
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

    static Pattern _wordPattern = Pattern.compile("\\w");

    private int findStartOfWord() {
        int position = _cursor.getPosition();
        if (!_wordPattern.matcher(getCharacter(position)).matches()) {
            return -1;
        }
        for (int i = position; i >= 0; --i) {
            if (!_wordPattern.matcher(getCharacter(i)).matches()) {
                return i + 1;
            }
        }
        return 0;
    }

    private int findEndOfWord() {
        int position = _cursor.getPosition();
        if (!_wordPattern.matcher(getCharacter(position)).matches()) {
            return -1;
        }
        for (int i = position; i < getLength(); ++i) {
            if (!_wordPattern.matcher(getCharacter(i)).matches()) {
                return i;
            }
        }
        return getLength();
    }

    public void deleteInnerWord() {
        int start = findStartOfWord();
        int end = findEndOfWord();
        if (start == -1 || end == -1) {
            return;
        }
        _string.delete(start, end);
        _cursor.setPosition(start);
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
