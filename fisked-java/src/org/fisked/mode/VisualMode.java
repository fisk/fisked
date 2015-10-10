package org.fisked.mode;

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
import org.fisked.responder.RecognitionState;
import org.fisked.services.ServiceManager;

public class VisualMode extends AbstractMode {
	private Cursor _activeCursor = _window.getBuffer().getCursor();
	private Cursor _inactiveCursor = Cursor.makeCursorFromCharIndex(_activeCursor.getCharIndex(), _window.getTextLayout());

	private Range getSelectionRange() {
		int cursor1 = _activeCursor.getCharIndex();
		int cursor2 = _inactiveCursor.getCharIndex();
		int minCursor = Math.min(cursor1, cursor2);
		int maxCursor = Math.max(cursor1, cursor2);
		int length = maxCursor - minCursor;
		Range selection = new Range(minCursor, length);
		return selection;
	}
	
	private void setSelection() {
		BufferController controller = _window.getBufferController();
		controller.setSelection(getSelectionRange());
	}
	
	private void clearSelection() {
		_window.getBufferController().setSelection(null);
	}

	public VisualMode(BufferWindow window) {
		super(window);

		addResponder(new CommandInputResponder(_window));
		addResponder(new BasicNavigationResponder(_window), () -> setSelection());
		addResponder(new NormalModeSwitchResponder(_window), () -> clearSelection());
		addResponder((Event nextEvent) -> {
			if (nextEvent.isCharacter() && nextEvent.getCharacter() == 'o') {
				Cursor other = _inactiveCursor;
				_window.getBuffer().setCursor(other);
				_inactiveCursor = _activeCursor;
				_activeCursor = other;
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder((Event nextEvent) -> {
			if (nextEvent.isCharacter() && nextEvent.getCharacter() == 'd') {
				Range selection = getSelectionRange();
				_window.getBuffer().removeCharsInRange(selection);
				clearSelection();
				_window.switchToNormalMode();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
		addResponder((Event nextEvent) -> {
			if (nextEvent.isCharacter() && nextEvent.getCharacter() == 'y') {
				Range selection = getSelectionRange();
				String string = _window.getBuffer().getStringBuilder().substring(selection.getStart(), selection.getEnd());
				setClipboard(string);
				clearSelection();
				_window.switchToNormalMode();
				return RecognitionState.Recognized;
			}
			return RecognitionState.NotRecognized;
		});
	}

	public void setClipboard(String text) {
		ServiceManager.getInstance().getClipboardService().setClipboard(text);
	}
	
	public void activate() {
		changeCursor(CURSOR_UNDERLINE);
	}
	
	public Face getModelineFace() {
		return new Face(Color.YELLOW, Color.WHITE);
	}

	@Override
	public String getModeName() {
		return "visual";
	}

}
