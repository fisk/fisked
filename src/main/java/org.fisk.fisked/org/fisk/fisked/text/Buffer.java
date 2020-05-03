package org.fisk.fisked.text;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.SemanticHighlightingInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.util.SemanticHighlightingTokens;
import org.fisk.fisked.EventThread;
import org.fisk.fisked.event.RunnableEvent;
import org.fisk.fisked.lsp.java.JavaLSPClient;
import org.fisk.fisked.ui.Cursor;
import org.fisk.fisked.ui.Window;
import org.fisk.fisked.undo.UndoLog;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.TextColor;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
    private Cursor _cursor;
    private BufferContext _bufferContext;
    private UndoLog _undoLog;
    private int _version = 1;
    private static Logger _log = LogFactory.createLog();

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
            var decoration = new Decoration();
            decoration._str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
            decoration._version = _version;
            _decorations.add(decoration);
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
        _version++;
        var decoration = new Decoration();
        decoration._str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
        decoration._version = _version;
        decoration._didInsert = true;
        decoration._insertPosition = position;
        decoration._insertString = str;
        _decorations.add(decoration);
        if (isJava()) {
            JavaLSPClient.getInstance().didInsert(_bufferContext, position, str);
        }
    }

    public void rawRemove(int startPosition, int endPosition) {
        _string.delete(startPosition, endPosition);
        _version++;
        var decoration = new Decoration();
        decoration._str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
        decoration._version = _version;
        decoration._didRemove = true;
        decoration._removeStart = startPosition;
        decoration._removeEnd = endPosition;
        _decorations.add(decoration);
        if (isJava()) {
            JavaLSPClient.getInstance().didRemove(_bufferContext, startPosition, endPosition);
        }
    }

    public void remove(int startPosition, int endPosition) {
        if (endPosition - startPosition <= 0) {
            return;
        }
        _undoLog.recordRemove(startPosition, endPosition);
        rawRemove(startPosition, endPosition);
        _cursor.setPosition(startPosition);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void insert(String str) {
        if (str.equals("\n")) {
            for (int i = 0; i < getIndentationLevel(); ++i) {
                str += Settings.getIndentationString();
            }
        }
        _undoLog.recordInsert(_cursor.getPosition(), str);
        rawInsert(_cursor.getPosition(), str);
        _bufferContext.getTextLayout().calculate();
        _cursor.setPosition(_cursor.getPosition() + str.length());
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void insert(int position, String str) {
        _undoLog.recordInsert(position, str);
        rawInsert(position, str);
        _bufferContext.getTextLayout().calculate();
        _cursor.setPosition(position + str.length());
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void removeBefore() {
        if (_cursor.getPosition() == 0 || _string.length() == 0) {
            return;
        }
        int position = _cursor.getPosition();
        _undoLog.recordRemove(position - 1, position);
        _cursor.goBack();
        rawRemove(_cursor.getPosition(), _cursor.getPosition() + 1);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void removeAt() {
        if (_string.length() == 0) {
            return;
        }
        int position = _cursor.getPosition();
        if (position >= _string.length()) {
            return;
        }
        _undoLog.recordRemove(position, position + 1);
        rawRemove(_cursor.getPosition(), _cursor.getPosition() + 1);
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
        rawRemove(start, end);
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
        rawRemove(start, end);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void deleteLine() {
        var textLayout = _bufferContext.getTextLayout();
        var line = textLayout.getPhysicalLineAt(_cursor.getPosition());
        int start = line.getStartPosition();
        var glyph = line.getLastGlyph();
        int end;
        if (glyph != null) {
            end = glyph.getPosition() + 1;
        } else {
            end = line.getStartPosition();
        }
        if (line.getNext() == null) {
            // Last line is special
            start = Math.max(0, start - 1);
        }
        _undoLog.recordRemove(start, end);
        rawRemove(start, end);
        _bufferContext.getTextLayout().calculate();
        _cursor.setPosition(start);
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
        if (isJava()) {
            JavaLSPClient.getInstance().willSave(_bufferContext);
        }
        try {
            Files.writeString(_path, _string.toString());
            Window.getInstance().getCommandView().setMessage("Saved file");
        } catch (IOException e) {
        }
        if (isJava()) {
            JavaLSPClient.getInstance().didSave(_bufferContext);
        }
    }
    
    public void close() {
        if (isJava()) {
            JavaLSPClient.getInstance().didClose(_bufferContext);
        }
    }
    
    public void open() {
        if (isJava()) {
            JavaLSPClient.getInstance().didOpen(_bufferContext);
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

    public URI getURI() {
        return _path.toFile().toURI();
    }

    private static Pattern _bracketPattern = Pattern.compile("\\{|\\}");
    public int getIndentationLevel() {
        int indentation = 0;
        if (isJava()) {
            int cursor = getCursor().getPosition();
            var matcher = _bracketPattern.matcher(_string.toString());
            while (matcher.find()) {
                if (matcher.start() >= cursor) {
                    return indentation;
                }
                if (matcher.group(0).equals("{")) {
                    ++indentation;
                }
                if (matcher.group(0).equals("}")) {
                    --indentation;
                }
            }
        }
        return indentation;
    }
    
    private Boolean _isJava;
    private boolean isJava() {
        if (_isJava != null) {
            return _isJava;
        }
        String extension = "";
        String fileName = _path.getFileName().toString();

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension = fileName.substring(i+1);
        }
        _isJava = extension.equals("java");
        return _isJava;
    }
    
    private static class Decoration {
        private volatile AttributedString _str;
        private int _version;
        
        private boolean _didInsert;
        private int _insertPosition;
        private String _insertString;
        
        private boolean _didRemove;
        private int _removeStart;
        private int _removeEnd;
        
        private volatile boolean _isDecorated;
    }
    
    private CopyOnWriteArrayList<Decoration> _decorations = new CopyOnWriteArrayList<>();
    
    private static Pattern _javaCommentPattern = Pattern.compile("(/\\*([^*]|[\\n]|(\\*+([^*/]|[\\n])))*\\*+/)|(//.*)", Pattern.MULTILINE);
    private static Pattern _javaStringPattern = Pattern.compile("\\\"([^\\\"]|[\\n])*\\\"", Pattern.MULTILINE);
    private static Pattern _javaKeywordPattern = Pattern.compile(
            "(\\bprivate\\b)|(\\bprotected\\b)|(\\bpublic\\b)|(\\bstatic\\b)|(\\babstract\\b)|" + 
            "(\\bvoid\\b)|(\\bbyte\\b)|(\\bchar\\b)|(\\bboolean\\b)|(\\bshort\\b)|(\\bint\\b)|(\\blong\\b)|(\\bfloat\\b)|" + 
            "(\\bdouble\\b)|(\\bimplements\\b)|(\\bextends\\b)|(\\bclass\\b)|(\\benum\\b)|(\\bfinal\\b)|" + 
            "(\\btry\\b)|(\\bcatch\\b)|(\\bthrows\\b)|(\\bthrow\\b)|(\\brecord\\b)|(\\bnew\\b)|(\\breturn\\b)|" +
            "(\\bif\\b)|(\\bfor\\b)|(\\bwhile\\b)|(\\bdo\\b)|(\\bimport\\b)|(\\bpackage\\b)", Pattern.MULTILINE);
    private static Pattern _javaNullPattern = Pattern.compile("\\bnull\\b", Pattern.MULTILINE);
    
    private void applyJavaTokenColouring(AttributedString str) {
        var string = str.toString();
        formatToken(str, string, _javaKeywordPattern, TextColor.ANSI.RED);
        formatToken(str, string, _javaNullPattern, TextColor.ANSI.CYAN);
        formatToken(str, string, _javaCommentPattern, TextColor.ANSI.GREEN);
        formatToken(str, string, _javaStringPattern, TextColor.ANSI.YELLOW);
    }
    
    private void formatToken(AttributedString str, String string, Pattern pattern, TextColor colour) {
        var matcher = pattern.matcher(string);
        while (matcher.find()) {
            str.format(matcher.start(), matcher.end(), colour, TextColor.ANSI.DEFAULT);
        }
    }

    public void applyDecorations(int version, List<SemanticHighlightingInformation> info) {
        _log.info("Applying decorations for version " + version);
        AttributedString str = null;
        for (var decoration: _decorations) {
            if (decoration._isDecorated) {
                _log.info("Found decorated string for version " + decoration._version);
                str = AttributedString.create(decoration._str);
            } else if (str != null) {
                if (decoration._didInsert) {
                    _log.info("Inserting string for version " + decoration._version);
                    str.insert(decoration._insertString, decoration._insertPosition, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
                }
                if (decoration._didRemove) {
                    _log.info("Removing string for version " + decoration._version);
                    str.remove(decoration._removeStart, decoration._removeEnd);
                }
            }
            if (decoration._version != version) {
                _log.info("Skipping version " + decoration._version);
            } else {
                _log.info("Found version " + version);
                if (str != null) {
                    if (!decoration._str.toString().equals(str.toString())) {
                        throw new RuntimeException("Strings do not match: 1) " + decoration._str.toString() + "\n2) " + str.toString());
                    }
                    decoration._str = AttributedString.create(str);
                }
                _log.info("String length: " + decoration._str.length());
                for (var line: info) {
                    var decodedTokens = SemanticHighlightingTokens.decode(line.getTokens());
                    var lineNum = line.getLine();
                    for (var token: decodedTokens) {
                        var charNum = token.character;
                        int index = _bufferContext.getTextLayout().getIndexForPhysicalLineCharacter(lineNum, charNum);
                        _log.info("Format range [" + index + ", " + (index + token.length) + ")");
                        decoration._str.format(index, index + token.length, 
                                JavaLSPClient.getInstance().foregroundColourForScope(token.scope), 
                                TextColor.ANSI.DEFAULT);
                    }
                }
                if (isJava()) {
                    applyJavaTokenColouring(decoration._str);
                }
                decoration._isDecorated = true;
                EventThread.getInstance().enqueue(new RunnableEvent(() -> {
                    _log.info("Redrawing version " + version);
                    _bufferContext.getBufferView().setNeedsRedraw();
                }));
                break;
            }
        }
    }
    
    public AttributedString getAttributedString() {
        Decoration lastAttributedDecoration = null;
        for (var decoration: _decorations) {
            if (decoration._isDecorated) {
                lastAttributedDecoration = decoration;
            }
        }
        if (lastAttributedDecoration != null) {
            for (var decoration: _decorations) {
                if (decoration == lastAttributedDecoration) {
                    break;
                } else {
                    _decorations.remove(decoration);
                }
            }
            AttributedString str = null;
            for (var decoration: _decorations) {
                if (decoration._isDecorated) {
                    _log.info("Found decorated string for version " + decoration._version);
                    str = AttributedString.create(decoration._str);
                } else if (str != null) {
                    if (decoration._didInsert) {
                        _log.info("Inserting string for version " + decoration._version);
                        str.insert(decoration._insertString, decoration._insertPosition, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
                    }
                    if (decoration._didRemove) {
                        _log.info("Removing string for version " + decoration._version);
                        str.remove(decoration._removeStart, decoration._removeEnd);
                    }
                }
            }
            return str;
        } else {
            return AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
        }
    }

    public TextDocumentItem getTextDocument() {
        if (isJava()) {
            return new TextDocumentItem(_path.toFile().toURI().toString(), "java", 11, _string.toString());
        }
        return null;
    }

    public TextDocumentIdentifier getTextDocumentID() {
        return new TextDocumentIdentifier(_path.toFile().toURI().toString());
    }
    
    public VersionedTextDocumentIdentifier getVersionedTextDocumentID() {
        return new VersionedTextDocumentIdentifier(_path.toFile().toURI().toString(), _version);
    }
}
