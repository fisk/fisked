package org.fisk.fisked.lsp.latex;

import java.util.regex.Pattern;

import org.eclipse.lsp4j.TextDocumentItem;
import org.fisk.fisked.lsp.LanguageMode;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.BufferContext;

import com.googlecode.lanterna.TextColor;

public class LatexLSPClient implements LanguageMode {

    @Override
    public void didInsert(BufferContext bufferContext, int position, String str) {
    }

    @Override
    public void didRemove(BufferContext bufferContext, int startPosition, int endPosition) {
    }

    @Override
    public void willSave(BufferContext bufferContext) {
    }

    @Override
    public void didSave(BufferContext bufferContext) {
    }

    @Override
    public void didClose(BufferContext bufferContext) {
    }

    @Override
    public void didOpen(BufferContext bufferContext) {
    }

    @Override
    public int getIndentationLevel(BufferContext bufferContext) {
        return 0;
    }

    @Override
    public TextDocumentItem getTextDocument(BufferContext bufferContext) {
        return null;
    }
    
    private static Pattern _latexKeywordPattern = Pattern.compile("(\\\\\\b\\w+\\b)", Pattern.MULTILINE);
    private static Pattern _bracketKeywordPattern = Pattern.compile("\\{|\\}", Pattern.MULTILINE);

    private void formatToken(AttributedString str, String string, Pattern pattern, TextColor colour) {
        var matcher = pattern.matcher(string);
        while (matcher.find()) {
            str.format(matcher.start(), matcher.end(), colour, TextColor.ANSI.DEFAULT);
        }
    }

    @Override
    public void applyColouring(BufferContext bufferContext, AttributedString str) {
        var string = str.toString();
        formatToken(str, string, _latexKeywordPattern, TextColor.ANSI.RED);
        formatToken(str, string, _bracketKeywordPattern, TextColor.ANSI.RED);
    }

}
