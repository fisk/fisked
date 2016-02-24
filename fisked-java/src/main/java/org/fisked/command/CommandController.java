package org.fisked.command;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.ICommandManager;
import org.fisked.renderingengine.service.models.Rectangle;
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

	private void handleCommand(String command) {
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

}
