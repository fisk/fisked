package org.fisked.responder.motion;

import org.fisked.renderingengine.service.models.Range;
import org.fisked.responder.IInputRecognizer;

public interface IMotion extends IInputRecognizer {
	public class MotionRange {
		private final int _start;
		private final int _end;
		
		public MotionRange(int start, int end) {
			_start = start;
			_end = end;
		}
		
		public int getStart() {
			return _start;
		}
		
		public int getEnd() {
			return _end;
		}
		
		public Range getRange() {
			int min = Math.min(_start, _end);
			int max = Math.max(_start, _end);
			return new Range(min, max - min);
		}
	}
	
	MotionRange getMotionRange();
}
