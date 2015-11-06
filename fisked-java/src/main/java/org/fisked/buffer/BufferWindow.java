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
	private final ModeLineController _modeLineController;
	private final ModeLineView _modeLineView;

	private final CommandController _commandController;
	private final CommandView _commandView;

	private final LineNumberController _lineNumberController;
	private final LineNumberView _lineNumberView;

	private final BufferView _bufferView;
	private final BufferController _bufferController;

	private AbstractMode _currentMode;
	private AbstractMode _recognizedMode;

	public BufferWindow(Rectangle windowRect) {
		super(windowRect);

		int widthForLineNumbers = Settings.getInstance().getNumberOfDigitsForLineNumbers();
		if (widthForLineNumbers > 0) {
			widthForLineNumbers++;
		}

		Rectangle rootViewRect = windowRect;
		Rectangle modeLineRect = new Rectangle(0, rootViewRect.getSize().getHeight() - 2,
				rootViewRect.getSize().getWidth(), 1);
		Rectangle commandLineRect = new Rectangle(0, rootViewRect.getSize().getHeight() - 1,
				rootViewRect.getSize().getWidth(), 1);
		Rectangle lineNumberRect = new Rectangle(0, 0, widthForLineNumbers, rootViewRect.getSize().getHeight() - 2);
		Rectangle bufferViewRect = new Rectangle(widthForLineNumbers, 0,
				rootViewRect.getSize().getWidth() - lineNumberRect.getSize().getWidth(),
				rootViewRect.getSize().getHeight() - 2);
		_rootView = new View(rootViewRect);

		_modeLineController = new ModeLineController(this);
		_modeLineView = new ModeLineView(modeLineRect, _modeLineController);

		_commandController = new CommandController(this);
		_commandView = new CommandView(commandLineRect, _commandController);

		_bufferView = new BufferView(bufferViewRect);
		_bufferController = new BufferController(_bufferView, bufferViewRect.getSize());
		_bufferView.setBufferController(_bufferController);

		_lineNumberController = new LineNumberController(this);
		_lineNumberView = new LineNumberView(lineNumberRect, _lineNumberController);

		_rootView.addSubview(_lineNumberView);
		_rootView.addSubview(_bufferView);
		_rootView.addSubview(_modeLineView);
		_rootView.addSubview(_commandView);

		_currentMode = new NormalMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		_recognizedMode = _currentMode;
		RecognitionState result = _recognizedMode.recognizesInput(input);
		return result;
	}

	@Override
	public void onRecognize() {
		_recognizedMode.onRecognize();
	}

	public Buffer getBuffer() {
		return _bufferController.getBuffer();
	}

	public void setBuffer(Buffer buffer) {
		_bufferController.setBuffer(buffer);
		setNeedsFullRedraw();
	}

	public void openFile(File file) throws IOException {
		try {
			file.createNewFile();
			Buffer buffer = new Buffer(file);
			setBuffer(buffer);
			_currentMode = null;
			switchToNormalMode();
			setNeedsFullRedraw();
		} catch (IOException e) {
			getCommandController().setCommandFeedback("Could not open file: " + file.getCanonicalPath() + ".");
			setNeedsFullRedraw();
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
		if (_currentMode != null && _currentMode instanceof InputMode)
			return;
		_currentMode = new InputMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
	}

	public void switchToNormalMode() {
		if (_currentMode != null && _currentMode instanceof NormalMode)
			return;
		_currentMode = new NormalMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
	}

	public void switchToVisualMode() {
		if (_currentMode != null && _currentMode instanceof VisualMode)
			return;
		_currentMode = new VisualMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
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
