package org.fisked.responder.register;

import org.fisked.buffer.registers.RegisterManager;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.RecognitionState;

public class RegisterRecognizer implements IRegisterRecognizer {
	private char _matchedRegister = RegisterManager.UNNAMED_REGISTER;

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		EventRecognition prefixState = EventRecognition.matchesRecognition(nextEvent, "\"");
		if (prefixState.getState() == RecognitionState.Recognized) {
			Event spillEvent = prefixState.getSpillEvent();
			if (spillEvent == null) {
				return RecognitionState.MaybeRecognized;
			}
			if (spillEvent.isCharacter()) {
				char register = spillEvent.getCharacter();
				_matchedRegister = register;
				return RecognitionState.Recognized;
			} else {
				_matchedRegister = RegisterManager.UNNAMED_REGISTER;
				return RecognitionState.NotRecognized;
			}
		} else {
			_matchedRegister = RegisterManager.UNNAMED_REGISTER;
			return prefixState.getState();
		}
	}

	@Override
	public char getRegister() {
		return _matchedRegister;
	}

}
