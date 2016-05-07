/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
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
package org.fisked.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.ICommandManager;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.renderingengine.service.models.selection.Selection;
import org.fisked.renderingengine.service.models.selection.SelectionMode;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandController implements IInputRecognizer {
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(CommandController.class);
	private final static Logger LOG = LoggerFactory.getLogger(CommandController.class);
	private final BufferWindow _window;
	private StringBuilder _command;
	private boolean _writingCommand;
	private String _feedback;

	public CommandController(BufferWindow bufferWindow) {
		_window = bufferWindow;
		_command = new StringBuilder();
		_writingCommand = false;
	}

	public String getString(Rectangle drawingRect) {
		if (_feedback != null) {
			return _feedback;
		} else if (_writingCommand) {
			return ":" + _command.toString();
		} else {
			return "";
		}
	}

	public void setCommandFeedback(String feedback) {
		_feedback = feedback;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		if (nextEvent.isBackspace()) {
			if (_command.length() > 0) {
				_command.deleteCharAt(_command.length() - 1);
			}
		} else if (nextEvent.isReturn()) {
			finishCommand();
			return RecognitionState.NotRecognized;
		} else {
			_command.append(nextEvent.getCharacter());
		}
		return RecognitionState.Recognized;
	}

	public void startCommand() {
		_writingCommand = true;
		_feedback = null;
	}

	private void finishCommand() {
		String command = _command.toString();
		_writingCommand = false;
		_command = new StringBuilder();
		handleCommand(command);
	}

	Pattern searchReplacePattern = Pattern.compile("s/(.*)/(.*)/g?");

	private void handleCommand(String command) {
		Matcher searchPattern = searchReplacePattern.matcher(command);
		if (searchPattern.matches()) {
			handleSearchReplace(searchPattern);
			return;
		}
		String[] argv = command.split("\\s+");
		if (argv.length >= 1) {
			try (IBehaviorConnection<ICommandManager> commandBC = BEHAVIORS.getBehaviorConnection(ICommandManager.class)
					.get()) {
				commandBC.getBehavior().handle(_window, argv[0], argv);
			} catch (Exception e) {
				LOG.error("Command failed: ", e);
			}
		}
	}

	private void handleSearchReplace(Matcher searchPattern) {
		String searchString = searchPattern.group(1);
		String replaceString = searchPattern.group(2);

		int adjust = 0;
		String bufferString = _window.getBuffer().toString();
		Selection selection = _window.getBufferController().getSelection();

		if (selection != null) {
			Range range = selection.getRange();
			if (selection.getMode() != SelectionMode.NORMAL_MODE)
				throw new RuntimeException("Not yet implemented");
			bufferString = bufferString.substring(range.getStart(), range.getEnd());
			adjust += range.getStart();
		}

		Pattern pattern = Pattern.compile(searchString);
		Matcher matcher = pattern.matcher(bufferString);

		int position = -1;

		Buffer buffer = _window.getBuffer();
		buffer.pushUndoScope();

		while (matcher.find()) {
			int start = matcher.start(0) + adjust;
			int end = matcher.end(0) + adjust;
			buffer.removeCharsInRangeLogged(new Range(start, end - start));
			buffer.insertStringLogged(start, replaceString);
			adjust += replaceString.length() - (end - start);
			position = start;
		}

		buffer.popUndoScope();

		if (position >= 0) {
			buffer.getCursor().setCharIndex(position, true);
			_window.getBufferController().setSelection(null);
		}
	}

}
