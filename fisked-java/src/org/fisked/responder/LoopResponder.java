package org.fisked.responder;

import org.fisked.responder.NumberPrefixResponder.INumberInputResponder;

public class LoopResponder implements IInputResponder {
	private NumberPrefixResponder _prefix;
	
	public LoopResponder(IInputResponder responder) {
		_prefix = new NumberPrefixResponder(new INumberInputResponder() {
			@Override
			public RecognitionState recognizesInput(Event nextEvent) {
				return responder.recognizesInput(nextEvent);
			}

			@Override
			public void onRecognize() {
				responder.onRecognize();
			}

			@Override
			public void onRecognize(int number) {
				for (int i = 0; i < number; i++) {
					responder.onRecognize();
				}
			}
		});
	}
	
	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return _prefix.recognizesInput(nextEvent);
	}

	@Override
	public void onRecognize() {
		_prefix.onRecognize();
	}
}
