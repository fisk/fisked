package org.fisked.mode;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.util.InputController;
import org.fisked.mode.util.InputController.CommandControllerDelegate;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.responder.Event;

public class VisualMode extends AbstractMode implements CommandControllerDelegate {
	private InputController _inputController = new InputController();
	
	private class Cursor {
		int _charIndex;
	}
	
	private Cursor _cursorOne = new Cursor();
	private Cursor _cursorTwo = new Cursor();
	private Cursor _activeCursor = _cursorOne;
	
	public VisualMode(BufferWindow window) {
		super(window);
	}

	public void setClipboard(String text) {
		StringSelection selection = new StringSelection(text);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}

	@Override
	public boolean handleInput(Event nextEvent) {
		_inputController.handleInput(nextEvent, _window, this);
		return true;
	}

	@Override
	public String getModeName() {
		return "visual";
	}

	@Override
	public void handleCommand(Event input) {
		if (input.isEscape()) {
			_window.switchToNormalMode();
		}
	}

}
