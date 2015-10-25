package org.fisked.responder.motion;

import org.fisked.responder.IInputRecognizer;

public interface IMotion extends IInputRecognizer {
	public class MotionRange {
		private int _start;
		private int _end;
		
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
	}
	
	MotionRange getRange();
}
