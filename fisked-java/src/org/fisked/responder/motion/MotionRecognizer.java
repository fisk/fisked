package org.fisked.responder.motion;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.BufferWindow;
import org.fisked.log.Log;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;

public class MotionRecognizer implements IMotion {
	private List<IMotion> _motions = new ArrayList<IMotion>();
	private IMotion _match = null;
	
	public MotionRecognizer(BufferWindow window) {
		_motions.add(new FindMotion(window));
	}

	@Override
	public RecognitionState handleInput(Event nextEvent) {
		boolean maybe = false;
		for (IMotion motion : _motions) {
			RecognitionState state = motion.handleInput(nextEvent);
			if (state == RecognitionState.Recognized) {
				_match = motion;
				return state;
			} else if (state == RecognitionState.MaybeRecognized) {
				maybe = true;
			}
		}
		return maybe ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}

	@Override
	public MotionRange getRange() {
		MotionRange range = _match.getRange();
		_match = null;
		return range;
	}

}
