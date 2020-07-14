package org.fisk.fisked.lsp;

import org.eclipse.lsp4j.TextDocumentItem;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.BufferContext;

public interface LanguageMode {
    void didInsert(BufferContext bufferContext, int position, String str);
    void didRemove(BufferContext bufferContext, int startPosition, int endPosition);
    void willSave(BufferContext bufferContext);
    void didSave(BufferContext bufferContext);
    void didClose(BufferContext bufferContext);
    void didOpen(BufferContext bufferContext);
    int getIndentationLevel(BufferContext bufferContext);
    TextDocumentItem getTextDocument(BufferContext bufferContext);
    void applyColouring(BufferContext bufferContext, AttributedString str);
}
