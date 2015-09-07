package org.fisked.buffer;

import org.fisked.buffer.drawing.Point;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.buffer.drawing.Window;
import org.fisked.command.CommandController;
import org.fisked.command.CommandView;
import org.fisked.log.Log;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.InputMode;
import org.fisked.mode.NormalMode;
import org.fisked.responder.Event;

import jcurses.system.Toolkit;

public class BufferWindow extends Window {
	private ModeLineController _modeLineController;
	private ModeLineView _modeLineView;
	
	private CommandController _commandController;
	private CommandView _commandView;
	
	private BufferView _bufferView;
	private BufferController _bufferController;
	
	private AbstractMode _currentMode;

	public BufferWindow(Rectangle windowRect) {
		super(windowRect);
		
		Rectangle rootViewRect = windowRect;
		Rectangle modeLineRect = new Rectangle(
				0, rootViewRect.getSize().getHeight() - 2,
				rootViewRect.getSize().getWidth(), 1
				);
		Rectangle commandLineRect = new Rectangle(
				0, rootViewRect.getSize().getHeight() - 1,
				rootViewRect.getSize().getWidth(), 1
				);
		Rectangle bufferViewRect = new Rectangle(
				0, 0,
				rootViewRect.getSize().getWidth(), rootViewRect.getSize().getHeight() - 2
				);
		_rootView = new View(rootViewRect);
		_currentMode = new NormalMode(this);
		
		_modeLineController = new ModeLineController(this);
		_modeLineView = new ModeLineView(modeLineRect, _modeLineController);
		
		_commandController = new CommandController(this);
		_commandView = new CommandView(commandLineRect, _commandController);
		
		_bufferView = new BufferView(bufferViewRect);
		_bufferController = new BufferController(_bufferView);
		_bufferView.setBufferController(_bufferController);
		
		_rootView.addSubview(_bufferView);
		_rootView.addSubview(_modeLineView);
		_rootView.addSubview(_commandView);
	}
	
	@Override
	public boolean handleInput(Event input) {
		return _currentMode.handleInput(input);
	}

	public Buffer getBuffer() {
		return _bufferController.getBuffer();
	}
	
	public void setBuffer(Buffer buffer) {
		_bufferController.setBuffer(buffer);
		refresh();
	}
	
	public CommandController getCommandController() {
		return _commandController;
	}
	
	@Override
	public void drawPoint() {
		Point point = _bufferController.getLogicalPoint();
		Toolkit.move(point.getX(), point.getY());
	}

	public void switchToInputMode() {
		_currentMode = new InputMode(this);
		refresh();
	}

	public void switchToNormalMode() {
		_currentMode = new NormalMode(this);
		refresh();
	}
	
	public AbstractMode getCurrentMode() {
		return _currentMode;
	}
	
	public void refresh() {
		draw();
		drawPoint();
	}

}
