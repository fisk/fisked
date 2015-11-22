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
import org.fisked.responder.EventRecognition;
import org.fisked.services.ServiceManager;

public class VisualMode extends AbstractMode {
	private Cursor _activeCursor = _window.getBuffer().getCursor();
	private Cursor _inactiveCursor = Cursor.makeCursorFromCharIndex(_activeCursor.getCharIndex(),
			_window.getTextLayout());

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
		_window.setNeedsFullRedraw();
	}

	private void clearSelection() {
		_window.getBufferController().setSelection(null);
	}

	public VisualMode(BufferWindow window) {
		super(window);

		addResponder(new CommandInputResponder(_window));
		NormalModeSwitchResponder normalModeSwitch = new NormalModeSwitchResponder(_window);
		addResponder(normalModeSwitch, () -> {
			normalModeSwitch.onRecognize();
			clearSelection();
		});
		BasicNavigationResponder navigationResponder = new BasicNavigationResponder(_window);
		addResponder(navigationResponder, () -> {
			navigationResponder.onRecognize();
			setSelection();
		});
		addResponder((Event nextEvent) -> {
			return EventRecognition.matchesExact(nextEvent, "o");
		} , () -> {
			Cursor other = _inactiveCursor;
			_window.getBuffer().setCursor(other);
			_inactiveCursor = _activeCursor;
			_activeCursor = other;
		});
		addResponder((Event nextEvent) -> {
			return EventRecognition.matchesExact(nextEvent, "d");
		} , () -> {
			Range selection = getSelectionRange();
			_window.getBuffer().removeCharsInRangeLogged(selection);
			clearSelection();
			_window.switchToNormalMode();
		});
		addResponder((Event nextEvent) -> {
			return EventRecognition.matchesExact(nextEvent, "c");
		} , () -> {
			Range selection = getSelectionRange();
			_window.getBuffer().removeCharsInRangeLogged(selection);
			clearSelection();
			_window.switchToInputMode(0);
		});
		addResponder((Event nextEvent) -> {
			return EventRecognition.matchesExact(nextEvent, "y");
		} , () -> {
			Range selection = getSelectionRange();
			CharSequence string = _window.getBuffer().getCharSequence().subSequence(selection.getStart(),
					selection.getEnd());
			setClipboard(string.toString());
			clearSelection();
			_window.switchToNormalMode();
		});
	}

	public void setClipboard(String text) {
		ServiceManager.getInstance().getClipboardService().setClipboard(text);
	}

	@Override
	public void activate() {
		changeCursor(CURSOR_UNDERLINE);
	}

	@Override
	public Face getModelineFace() {
		return new Face(Color.YELLOW, Color.WHITE);
	}

	@Override
	public String getModeName() {
		return "visual";
	}

}
