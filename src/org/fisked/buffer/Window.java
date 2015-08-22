package org.fisked.buffer;

import org.fisked.buffer.drawing.IDrawable;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.NormalMode;
import org.fisked.responder.IRawInputResponder;

import jcurses.system.CharColor;
import jcurses.system.InputChar;
import jcurses.system.Toolkit;

public class Window implements IRawInputResponder, IDrawable {
	private AbstractMode _currentMode;
	private Buffer _buffer;
	private ModeLine _modeLine;
	private View _bufferView;
	
	public Window(Rectangle windowRect) {
		_buffer = new Buffer();
		_currentMode = new NormalMode(this);
		_modeLine = new ModeLine();
		_bufferView = new BufferView(_buffer, windowRect);
	}
	
	public Buffer getBuffer() {
		return _buffer;
	}

	@Override
	public boolean handleInput(InputChar input) {
		_currentMode.handleInput(input);
		return true;
	}

	@Override
	public void draw() {
		_bufferView.draw();
	}
	
	public void drawPoint() {
		Toolkit.move(_buffer.getPointIndex(), 0);
	}
	
}
