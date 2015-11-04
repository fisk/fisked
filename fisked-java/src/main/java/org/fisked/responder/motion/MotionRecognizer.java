package org.fisked.responder.motion;

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.BufferWindow;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;

public class MotionRecognizer implements IMotion {
	private final List<IMotion> _motions = new ArrayList<IMotion>();
	private IMotion _match = null;

	public MotionRecognizer(BufferWindow window) {
		_motions.add(new FindMotion(window));
		_motions.add(new BufferStartMotion(window));
		_motions.add(new BufferEndMotion(window));
		_motions.add(new GoToLineMotion(window));
		_motions.add(new LineStartMotion(window));
		_motions.add(new LineEndMotion(window));
		_motions.add(new NextWordStartMotion(window));
		_motions.add(new NextWordEndMotion(window));
		_motions.add(new PreviousWordStartMotion(window));
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		boolean maybe = false;
		for (IMotion motion : _motions) {
			RecognitionState state = motion.recognizesInput(nextEvent);
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
	public MotionRange getMotionRange() {
		MotionRange range = _match.getMotionRange();
		return range;
	}

}