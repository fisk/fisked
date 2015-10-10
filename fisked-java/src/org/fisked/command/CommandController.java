package org.fisked.command;

import org.fisked.buffer.BufferWindow;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;

public class CommandController implements IInputResponder {
	private BufferWindow _window;
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
	public RecognitionState handleInput(Event nextEvent) {
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
	
	private void handleCommand(String command) {
		String[] argv = command.split("\\s+");
		if (argv.length >= 1) {
			CommandManager.getSingleton().handle(_window, argv[0], argv);
		}
	}

}
