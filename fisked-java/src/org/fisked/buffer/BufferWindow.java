package org.fisked.buffer;

import java.io.File;
import java.io.IOException;

import org.fisked.buffer.drawing.View;
import org.fisked.buffer.drawing.Window;
import org.fisked.command.CommandController;
import org.fisked.command.CommandView;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.InputMode;
import org.fisked.mode.NormalMode;
import org.fisked.mode.VisualMode;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;
import org.fisked.settings.Settings;
import org.fisked.text.TextLayout;

public class BufferWindow extends Window {
	private ModeLineController _modeLineController;
	private ModeLineView _modeLineView;
	
	private CommandController _commandController;
	private CommandView _commandView;
	
	private LineNumberController _lineNumberController;
	private LineNumberView _lineNumberView;
	
	private BufferView _bufferView;
	private BufferController _bufferController;
	
	private AbstractMode _currentMode;

	public BufferWindow(Rectangle windowRect) {
		super(windowRect);
		
		final int numberOfDigitsForLineNumbers = Settings.getInstance().getNumberOfDigitsForLineNumbers();
		
		Rectangle rootViewRect = windowRect;
		Rectangle modeLineRect = new Rectangle(
				0, rootViewRect.getSize().getHeight() - 2,
				rootViewRect.getSize().getWidth(), 1
				);
		Rectangle commandLineRect = new Rectangle(
				0, rootViewRect.getSize().getHeight() - 1,
				rootViewRect.getSize().getWidth(), 1
				);
		Rectangle lineNumberRect = new Rectangle(
				0, 0,
				numberOfDigitsForLineNumbers + 1, rootViewRect.getSize().getHeight() - 2
				);
		Rectangle bufferViewRect = new Rectangle(
				5, 0,
				rootViewRect.getSize().getWidth() - lineNumberRect.getSize().getWidth(), rootViewRect.getSize().getHeight() - 2
				);
		_rootView = new View(rootViewRect);
		
		_modeLineController = new ModeLineController(this);
		_modeLineView = new ModeLineView(modeLineRect, _modeLineController);
		
		_commandController = new CommandController(this);
		_commandView = new CommandView(commandLineRect, _commandController);
		
		_bufferView = new BufferView(bufferViewRect);
		_bufferController = new BufferController(_bufferView, bufferViewRect.getSize());
		_bufferView.setBufferController(_bufferController);
		
		_lineNumberController = new LineNumberController(this, numberOfDigitsForLineNumbers);
		_lineNumberView = new LineNumberView(lineNumberRect, _lineNumberController);
		
		_rootView.addSubview(_lineNumberView);
		_rootView.addSubview(_bufferView);
		_rootView.addSubview(_modeLineView);
		_rootView.addSubview(_commandView);

		_currentMode = new NormalMode(this);
		_currentMode.activate();
	}
	
	@Override
	public RecognitionState handleInput(Event input) {
		RecognitionState result = _currentMode.handleInput(input);
		setNeedsRedraw();
		return result;
	}

	public Buffer getBuffer() {
		return _bufferController.getBuffer();
	}
	
	public void setBuffer(Buffer buffer) {
		_bufferController.setBuffer(buffer);
		setNeedsRedraw();
	}
	
	public void openFile(File file) throws IOException {
		try {
			file.createNewFile();
			Buffer buffer = new Buffer(file);
			setBuffer(buffer);
			switchToNormalMode();
			setNeedsRedraw();
		} catch (IOException e) {
			getCommandController().setCommandFeedback("Could not open file: " + file.getCanonicalPath() + ".");
			setNeedsRedraw();
		}
	}
	
	public CommandController getCommandController() {
		return _commandController;
	}
	
	@Override
	public void drawPoint(IRenderingContext context) {
		Point point = _bufferController.getLogicalPoint();
		point = point.addedBy(_bufferView.getClippingRect().getOrigin());
		context.moveTo(point.getX(), point.getY());
	}

	public void switchToInputMode() {
		_currentMode = new InputMode(this);
		_currentMode.activate();
	}

	public void switchToNormalMode() {
		_currentMode = new NormalMode(this);
		_currentMode.activate();
	}

	public void switchToVisualMode() {
		_currentMode = new VisualMode(this);
		_currentMode.activate();
	}
	
	public AbstractMode getCurrentMode() {
		return _currentMode;
	}

	public TextLayout getTextLayout() {
		return _bufferController.getTextLayout();
	}

	public BufferController getBufferController() {
		return _bufferController;
	}

}
