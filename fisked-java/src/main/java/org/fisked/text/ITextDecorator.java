package org.fisked.text;

import org.fisked.buffer.BufferTextState;
import org.fisked.renderingengine.service.models.AttributedString;

public interface ITextDecorator {
	public interface ITextDecoratorCallback {
		void call(AttributedString string);
	}

	void decorate(BufferTextState state, ITextDecoratorCallback callback);
}
