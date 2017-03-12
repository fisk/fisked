/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.ui.buffer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.controller.BufferCommandController;
import org.fisked.buffer.controller.BufferController;
import org.fisked.buffer.controller.LineNumberController;
import org.fisked.buffer.controller.ModeLineController;
import org.fisked.command.CommandController;
import org.fisked.command.CommandView;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.InputMode;
import org.fisked.mode.NormalMode;
import org.fisked.mode.VisualMode;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;
import org.fisked.settings.Settings;
import org.fisked.text.TextLayout;
import org.fisked.ui.drawing.View;
import org.fisked.ui.window.Window;
import org.fisked.util.models.Point;
import org.fisked.util.models.Range;
import org.fisked.util.models.Rectangle;
import org.fisked.util.models.selection.SelectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferWindow extends Window {
	private final static Logger LOG = LoggerFactory.getLogger(BufferWindow.class);

	private final ModeLineController _modeLineController;
	private final ModeLineView _modeLineView;

	private final BufferCommandController _commandController;
	private final CommandView _commandView;

	private final LineNumberController _lineNumberController;
	private final LineNumberView _lineNumberView;

	private final BufferView _bufferView;
	private final BufferController _bufferController;

	private AbstractMode _currentMode;
	private AbstractMode _recognizedMode;

	public BufferWindow(Rectangle windowRect, String name) {
		super(windowRect, name);

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
		_modeLineView.setAutoResizeMask(View.AUTORESIZE_MASK_LEFT | View.AUTORESIZE_MASK_RIGHT
				| View.AUTORESIZE_MASK_BOTTOM | View.AUTORESIZE_MASK_HORIZONTAL);

		_commandController = new BufferCommandController(this);
		_commandView = new CommandView(commandLineRect, _commandController);
		_commandView.setAutoResizeMask(View.AUTORESIZE_MASK_LEFT | View.AUTORESIZE_MASK_RIGHT
				| View.AUTORESIZE_MASK_BOTTOM | View.AUTORESIZE_MASK_HORIZONTAL);

		_bufferView = new BufferView(bufferViewRect);
		_bufferController = new BufferController(_bufferView, bufferViewRect.getSize());
		_bufferView.setBufferController(_bufferController);

		_lineNumberController = new LineNumberController(this);
		_lineNumberView = new LineNumberView(lineNumberRect, _lineNumberController);
		_lineNumberView.setAutoResizeMask(View.AUTORESIZE_MASK_LEFT | View.AUTORESIZE_MASK_BOTTOM
				| View.AUTORESIZE_MASK_TOP | View.AUTORESIZE_MASK_VERTICAL);

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
		_currentMode.deactivate();
		_currentMode = new NormalMode(this);
		_currentMode.activate();
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
		Point point = _bufferController.getPrimaryLogicalPoint();
		LOG.debug("Draw point: " + point);

		point = point.addedBy(_bufferView.getFrame().getOrigin());

		LOG.debug("Draw resolved point: " + point);

		context.moveTo(point);
	}

	public void switchToBlockInputMode(List<Range> ranges) {
		LOG.debug("Switching to input mode.");
		if (_currentMode != null && _currentMode instanceof InputMode)
			return;
		if (_currentMode != null)
			_currentMode.deactivate();
		_currentMode = new InputMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
	}

	public void switchToInputMode() {
		LOG.debug("Switching to input mode.");
		if (_currentMode != null && _currentMode instanceof InputMode)
			return;
		if (_currentMode != null)
			_currentMode.deactivate();
		_currentMode = new InputMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
	}

	public void switchToNormalMode() {
		LOG.debug("Switching to normal mode.");
		if (_currentMode != null && _currentMode instanceof NormalMode)
			return;
		if (_currentMode != null)
			_currentMode.deactivate();
		_currentMode = new NormalMode(this);
		_currentMode.activate();
		setNeedsFullRedraw();
	}

	public void switchToVisualMode(SelectionMode mode) {
		LOG.debug("Switching to visual mode.");
		if (_currentMode != null && _currentMode instanceof VisualMode)
			return;
		if (_currentMode != null)
			_currentMode.deactivate();
		_currentMode = new VisualMode(this, mode);
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

	public void collapseCursors() {

	}
}
