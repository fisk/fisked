package org.fisk.fisked.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.fisk.fisked.ui.Cursor;
import org.fisk.fisked.undo.UndoLog;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
    private Cursor _cursor;
    private BufferContext _bufferContext;
    private UndoLog _undoLog;

    public Buffer() {
    }

    public Cursor getCursor() {
        return _cursor;
    }

    public Buffer(Path path, BufferContext bufferContext) {
        _path = path;
        _bufferContext = bufferContext;
        _cursor = new Cursor(bufferContext);
        _undoLog = new UndoLog(bufferContext);
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

    public void undo() {
        int position = _undoLog.undo();
        if (position == -1) {
            return;
        }
        _cursor.setPosition(position);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void redo() {
        int position = _undoLog.redo();
        if (position == -1) {
            return;
        }
        _cursor.setPosition(position);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public UndoLog getUndoLog() {
        return _undoLog;
    }

    public void rawInsert(int position, String str) {
        _string.insert(position, str);
    }

    public void rawRemove(int startPosition, int endPosition) {
        _string.delete(startPosition, endPosition);
    }

    public void remove(int startPosition, int endPosition) {
        if (endPosition - startPosition <= 0) {
            return;
        }
        endPosition++;
        _undoLog.recordRemove(startPosition, endPosition);
        _string.delete(startPosition, endPosition);
        _cursor.setPosition(startPosition);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void insert(String str) {
        _undoLog.recordInsert(_cursor.getPosition(), str);
        _string.insert(_cursor.getPosition(), str);
        _bufferContext.getTextLayout().calculate();
        _cursor.goForward();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void removeBefore() {
        if (_cursor.getPosition() == 0 || _string.length() == 0) {
            return;
        }
        int position = _cursor.getPosition();
        _undoLog.recordRemove(position - 1, position);
        _cursor.goBack();
        _string.deleteCharAt(_cursor.getPosition());
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void removeAt() {
        if (_string.length() == 0) {
            return;
        }
        int position = _cursor.getPosition();
        _undoLog.recordRemove(position, position + 1);
        _string.deleteCharAt(_cursor.getPosition());
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void deleteInnerWord() {
        int start = findStartOfWord();
        int end = findEndOfWord();
        if (start == -1 || end == -1) {
            return;
        }
        _undoLog.recordRemove(start, end);
        _string.delete(start, end);
        _cursor.setPosition(start);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void deleteWord() {
        int start = _cursor.getPosition();
        if (!_wordPattern.matcher(getCharacter(start)).matches()) {
            return;
        }
        int end = findEndOfWord();
        if (end == -1) {
            return;
        }
        _undoLog.recordRemove(start, end);
        _string.delete(start, end);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
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

    public String getSubstring(int start, int end) {
        return _string.substring(start, end);
    }
}
