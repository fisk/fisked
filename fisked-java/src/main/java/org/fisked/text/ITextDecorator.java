package org.fisked.text;

import org.fisked.renderingengine.service.models.AttributedString;

public interface ITextDecorator {
	void setNeedsRedraw();

	AttributedString decorate(AttributedString string);
}
