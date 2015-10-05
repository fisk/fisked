package org.fisked.mode;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.fisked.buffer.BufferController;
import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.Cursor;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.NormalModeSwitchResponder;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.responder.Event;

public class VisualMode extends AbstractMode {
	private Cursor _activeCursor = _window.getBuffer().getCursor();
	private Cursor _inactiveCursor = Cursor.makeCursorFromCharIndex(_activeCursor.getCharIndex(), _window.getTextLayout());

	private void setSelection() {
		int cursor1 = _activeCursor.getCharIndex();
		int cursor2 = _inactiveCursor.getCharIndex();
		int minCursor = Math.min(cursor1, cursor2);
		int maxCursor = Math.max(cursor1, cursor2);
		int length = maxCursor - minCursor;
		Range selection = new Range(minCursor, length);
		BufferController controller = _window.getBufferController();
		controller.setSelection(selection);
	}
	
	private void clearSelection() {
		_window.getBufferController().setSelection(null);
	}

	public VisualMode(BufferWindow window) {
		super(window);

		addResponder(new BasicNavigationResponder(_window), () -> setSelection());
		addResponder(new CommandInputResponder(_window));
		addResponder(new NormalModeSwitchResponder(_window), () -> clearSelection());
		addResponder((Event nextEvent) -> {
			if (nextEvent.isCharacter() && nextEvent.getCharacter() == 'o') {
				Cursor other = _inactiveCursor;
				_window.getBuffer().setCursor(other);
				_inactiveCursor = _activeCursor;
				_activeCursor = other;
				return true;
			}
			return false;
		});
	}

	public void setClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}
	
	public Face getModelineFace() {
		return new Face(Color.YELLOW, Color.WHITE);
	}

	@Override
	public String getModeName() {
		return "visual";
	}

}
