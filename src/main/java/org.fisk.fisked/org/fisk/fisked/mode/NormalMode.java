package org.fisk.fisked.mode;

import org.fisk.fisked.copy.Copy;
import org.fisk.fisked.event.FancyJumpResponder;
import org.fisk.fisked.fileindex.FileIndex;
import org.fisk.fisked.lsp.java.JavaLSPClient;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.TextLayout.Glyph;
import org.fisk.fisked.ui.Window;

public class NormalMode extends Mode {
    private FancyJumpResponder _fancyJump;
    
    public NormalMode(Window window) {
        super("NORMAL", window);
        setupBasicResponders();
        setupNavigationResponders();
    }

    private void setupBasicResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        String leader = "<SPACE>";
        _fancyJump = new FancyJumpResponder(bufferContext, 'w');
        _rootResponder.addEventResponder(_fancyJump);
        _rootResponder.addEventResponder(leader + " e i", () -> {
            JavaLSPClient.getInstance().organizeImports(window.getBufferContext());
        });
        _rootResponder.addEventResponder(leader + " e f", () -> {
            JavaLSPClient.getInstance().makeFinal(window.getBufferContext());
        });
        _rootResponder.addEventResponder(leader + " e a", () -> {
            JavaLSPClient.getInstance().generateAccessors(window.getBufferContext());
        });
        _rootResponder.addEventResponder(leader + " e s", () -> {
            JavaLSPClient.getInstance().generateToString(window.getBufferContext());
        });
        _rootResponder.addEventResponder(leader + " e l", () -> {
            JavaLSPClient.getInstance().codeLens(window.getBufferContext());
        });
        _rootResponder.addEventResponder("i", () -> { window.switchToMode(window.getInputMode()); });
        _rootResponder.addEventResponder("v", () -> { window.switchToMode(window.getVisualMode()); });
        _rootResponder.addEventResponder("V", () -> { window.switchToMode(window.getVisualLineMode()); });
        _rootResponder.addEventResponder("<CTRL>-v", () -> { window.switchToMode(window.getVisualBlockMode()); });
        _rootResponder.addEventResponder("u", () -> { buffer.undo(); });
        _rootResponder.addEventResponder("<CTRL>-r", () -> {window.getBufferContext().getBuffer().redo(); });
        _rootResponder.addEventResponder("d i w", () -> {
            buffer.deleteInnerWord();
            buffer.getUndoLog().commit();
        });
        _rootResponder.addEventResponder("d w", () -> {
            buffer.deleteWord();
            buffer.getUndoLog().commit();
        });
        _rootResponder.addEventResponder("d d", () -> {
            buffer.deleteLine();
            buffer.getUndoLog().commit();
        });
        _rootResponder.addEventResponder("x", () -> {
            buffer.removeAt();
            buffer.getUndoLog().commit();
        });
        _rootResponder.addEventResponder("c i w", () -> {
            buffer.deleteInnerWord();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("c i w", () -> {
            buffer.deleteWord();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("a", () -> {
            window.switchToMode(window.getInputMode());
            buffer.getCursor().goRight();
        });
        _rootResponder.addEventResponder("A", () -> {
            window.switchToMode(window.getInputMode());
            cursor.goEndOfLine();
        });
        _rootResponder.addEventResponder("o", () -> {
            cursor.goEndOfLine();
            window.switchToMode(window.getInputMode());
            buffer.insert("\n");
        });
        _rootResponder.addEventResponder("O", () -> {
            cursor.goStartOfLine();
            cursor.goBack();
            boolean isFirst = cursor.getPosition() == 0;
            window.switchToMode(window.getInputMode());
            buffer.insert("\n");
            if (isFirst) {
                cursor.goBack();
            }
        });
        _rootResponder.addEventResponder("p", () -> {
            if (Copy.getInstance().isLine()) {
                cursor.goEndOfLine();
                cursor.goForward();
                buffer.insert(Copy.getInstance().getText());
                cursor.goBack();
            } else {
                cursor.goForward();
                buffer.insert(Copy.getInstance().getText());
            }
        });
        _rootResponder.addEventResponder("P", () -> {
            if (Copy.getInstance().isLine()) {
                cursor.goStartOfLine();
                buffer.insert(Copy.getInstance().getText());
            } else {
                buffer.insert(Copy.getInstance().getText());
            }
        });
        _rootResponder.addEventResponder("n", () -> {
            if (window.isShowingList()) {
                window.hideList();
            } else {
                window.showList(FileIndex.createFileList(), "Project Files");
            }
        });
        _rootResponder.addEventResponder(":", () -> {
            window.getCommandView().activate();
        });
    }

    @Override
    public void activate() {
        _window.getBufferContext().getBuffer().getUndoLog().commit();
    }
    
    @Override
    public AttributedString decorate(Glyph glyph, AttributedString character) {
        character = _fancyJump.decorate(glyph, character);
        return character;
    }
}
