package org.fisked.buffer.basic.utilities;

import org.fisked.buffer.BufferWindow;
import org.fisked.buffer.basic.test.EditBufferTextTest;
import org.fisked.responder.Event;
import org.fisked.responder.RecognitionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEventLoop {
	EventBuilder _builder;
	BufferWindow _window;
	private final static Logger LOG = LoggerFactory.getLogger(EditBufferTextTest.class);

	public TestEventLoop(EventBuilder builder, BufferWindow window) {
		_builder = builder;
		_window = window;
	}

	public void feedEvent(Event event) {
		_builder.add(event);
		event = _builder.build();
		RecognitionState status = _window.recognizesInput(event);
		switch (status) {
		case Recognized:
			_window.onRecognize();
			_builder.clear();
			LOG.debug("Recognized: " + event);
			break;
		case NotRecognized:
			_builder.clear();
			LOG.debug("Not recognized: " + event);
			break;
		case MaybeRecognized:
			LOG.debug("Maybe recognized: " + event);
			break;
		}
	}

	public void feedEvent(String str) {
		for (int i = 0; i < str.length(); i++) {
			char character = str.charAt(i);
			feedEvent(new Event(character));
		}
	}
}
