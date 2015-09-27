package org.fisked.mode;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.fisked.buffer.BufferWindow;
import org.fisked.mode.responder.BasicNavigationResponder;
import org.fisked.mode.responder.CommandInputResponder;
import org.fisked.mode.responder.NormalModeSwitchResponder;

public class VisualMode extends AbstractMode {
	
	private class Cursor {
		int _charIndex;
	}
	
	private Cursor _cursorOne = new Cursor();
	private Cursor _cursorTwo = new Cursor();
	private Cursor _activeCursor = _cursorOne;
	
	public VisualMode(BufferWindow window) {
		super(window);
		addRecognizer(new BasicNavigationResponder(_window));
		addRecognizer(new CommandInputResponder(_window));
		addRecognizer(new NormalModeSwitchResponder(_window));
	}

	public void setClipboard(String text) {
		StringSelection selection = new StringSelection(text);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}

	@Override
	public String getModeName() {
		return "visual";
	}

}
