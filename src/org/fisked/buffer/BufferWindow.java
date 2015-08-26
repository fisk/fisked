package org.fisked.buffer;

import org.fisked.buffer.drawing.Point;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.buffer.drawing.Window;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.InputMode;
import org.fisked.mode.NormalMode;
import org.fisked.responder.Event;

import jcurses.system.Toolkit;

public class BufferWindow extends Window {
	private ModeLineController _modeLineController;
	private ModeLineView _modeLineView;
	private Buffer _buffer;
	private BufferView _bufferView;
	private BufferController _bufferController;
	private AbstractMode _currentMode;

	public BufferWindow(Rectangle windowRect) {
		super(windowRect);
		Rectangle rootViewRect = windowRect;
		Rectangle modeLineRect = new Rectangle(
				0, rootViewRect.getSize().getHeight() - 1,
				rootViewRect.getSize().getWidth(), 1
				);
		Rectangle bufferViewRect = new Rectangle(
				0, 0,
				rootViewRect.getSize().getWidth(), rootViewRect.getSize().getHeight() - 1
				);
		_rootView = new View(rootViewRect);
		_buffer = new Buffer();
		_currentMode = new NormalMode(this);
		_modeLineController = new ModeLineController(this);
		_modeLineView = new ModeLineView(modeLineRect, _modeLineController);
		_bufferView = new BufferView(bufferViewRect);
		_bufferController = new BufferController(_buffer, _bufferView);
		_bufferView.setBufferController(_bufferController);
		
		_rootView.addSubview(_bufferView);
		_rootView.addSubview(_modeLineView);
	}
	
	@Override
	public boolean handleInput(Event input) {
		return _currentMode.handleInput(input);
	}

	public Buffer getBuffer() {
		return _buffer;
	}
	
	@Override
	public void drawPoint() {
		Point point = _bufferController.getLogicalPointAtIndex(_buffer.getPointIndex());
		Toolkit.move(point.getX(), point.getY());
	}

	public void switchToInputMode() {
		_currentMode = new InputMode(this);
		draw();
		drawPoint();
	}

	public void switchToNormalMode() {
		_currentMode = new NormalMode(this);
		draw();
		drawPoint();
	}
	
	public AbstractMode getCurrentMode() {
		return _currentMode;
	}

}
