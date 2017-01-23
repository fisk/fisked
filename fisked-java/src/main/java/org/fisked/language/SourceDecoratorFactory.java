/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
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
package org.fisked.language;

import java.lang.reflect.Method;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.fisked.text.IBufferDecorator;
import org.fisked.util.Singleton;
import org.fisked.util.models.AttributedString;

public class SourceDecoratorFactory {

	public static SourceDecoratorFactory getInstance() {
		return Singleton.getInstance(SourceDecoratorFactory.class);
	}

	public interface ParserFactory<ParserType> {
		ParserType create(CommonTokenStream tokens);
	}

	public interface LexerFactory<LexerType> {
		LexerType create(ANTLRInputStream stream);
	}

	public interface ParseTreeListenerFactory<LexerType, ParserType> {
		ParseTreeListener create(LexerType lexer, ParserType parser, CommonTokenStream stream, AttributedString string);
	}

	public <LexerType extends Lexer, ParserType extends Parser> IBufferDecorator createDecorator(
			LexerFactory<LexerType> lexerFactory, ParserFactory<ParserType> parserFactory,
			ParseTreeListenerFactory<LexerType, ParserType> visitorFactory, String rootRuleName) {
		return (state, callback) -> {
			try {
				LexerType lexer;
				ParserType parser;
				CommonTokenStream tokens;
				Method compilationUnitMethod;
				AttributedString string = new AttributedString(state.toString());
				ANTLRInputStream inputStream = new ANTLRInputStream(string.toString());

				lexer = lexerFactory.create(inputStream);
				tokens = new CommonTokenStream(lexer);
				parser = parserFactory.create(tokens);

				lexer.removeErrorListeners();
				parser.removeErrorListeners();

				compilationUnitMethod = parser.getClass().getDeclaredMethod(rootRuleName);
				ParserRuleContext tree = (ParserRuleContext) compilationUnitMethod.invoke(parser);

				ParseTreeWalker walker = new ParseTreeWalker();
				ParseTreeListener extractor = visitorFactory.create(lexer, parser, tokens, string);
				walker.walk(extractor, tree);

				callback.call(string);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}
}
