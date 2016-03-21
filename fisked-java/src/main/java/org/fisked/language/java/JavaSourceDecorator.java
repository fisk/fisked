package org.fisked.language.java;

import org.antlr.v4.runtime.CommonTokenStream;
import org.fisked.buffer.BufferTextState;
import org.fisked.language.SourceDecoratorFactory;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.text.IBufferDecorator;

public class JavaSourceDecorator implements IBufferDecorator {
	private final IBufferDecorator _delegate;

	public JavaSourceDecorator() {
		SourceDecoratorFactory factory = SourceDecoratorFactory.getInstance();
		_delegate = factory.createDecorator(stream -> {
			return new Java8Lexer(stream);
		} , tokens -> {
			return new Java8Parser(tokens);
		} , (Java8Lexer lexer, Java8Parser parser, CommonTokenStream tokens, AttributedString string) -> {
			return new JavaSyntaxDecoratorVisitor(lexer, parser, tokens, string);
		} , "compilationUnit");
	}

	@Override
	public void decorate(BufferTextState state, IBufferDecoratorCallback callback) {
		_delegate.decorate(state, callback);
	}
}
