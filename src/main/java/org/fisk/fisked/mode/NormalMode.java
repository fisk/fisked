package org.fisk.fisked.mode;

import org.fisk.fisked.copy.Copy;
import org.fisk.fisked.fileindex.FileIndex;
import org.fisk.fisked.lsp.java.JavaLSPClient;
import org.fisk.fisked.ui.ListView.ListItem;
import org.fisk.fisked.ui.Window;

public class NormalMode extends Mode {
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
        _rootResponder.addEventResponder("b", () -> {
            JavaLSPClient.getInstance().organizeImports(window.getBufferContext());
        });
        _rootResponder.addEventResponder("i", () -> { window.switchToMode(window.getInputMode()); });
        _rootResponder.addEventResponder("v", () -> { window.switchToMode(window.getVisualMode()); });
        _rootResponder.addEventResponder("V", () -> { window.switchToMode(window.getVisualLineMode()); });
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
            cursor.goBack();
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

    private static class MyListItem extends ListItem {
        private String _string;

        public MyListItem(String string) {
            _string = string;
        }

        @Override
        public String displayString() {
            return _string;
        }

        @Override
        public void onClick() {
        }
    }

    @Override
    public void activate() {
        _window.getBufferContext().getBuffer().getUndoLog().commit();
    }
}
