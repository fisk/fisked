package org.fisked.text;

import java.text.AttributedString;

public interface ITextDecorator {
	AttributedString decorate(CharSequence string);
}
