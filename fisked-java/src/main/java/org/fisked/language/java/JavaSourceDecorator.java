/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
