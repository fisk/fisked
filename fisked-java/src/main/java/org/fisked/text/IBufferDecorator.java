package org.fisked.text;

import org.fisked.buffer.BufferTextState;
import org.fisked.renderingengine.service.models.AttributedString;

public interface IBufferDecorator {
	public interface IBufferDecoratorCallback {
		void call(AttributedString string);
	}

	void decorate(BufferTextState state, IBufferDecoratorCallback callback);
}
