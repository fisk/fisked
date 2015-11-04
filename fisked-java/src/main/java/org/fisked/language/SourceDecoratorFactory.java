package org.fisked.language;

import java.lang.reflect.Method;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.text.ITextDecorator;
import org.fisked.util.Singleton;

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

	public <LexerType extends Lexer, ParserType extends Parser> ITextDecorator createDecorator(
			LexerFactory<LexerType> lexerFactory, ParserFactory<ParserType> parserFactory,
			ParseTreeListenerFactory<LexerType, ParserType> visitorFactory, String rootRuleName) {
		return new ITextDecorator() {
			LexerType _lexer;
			ParserType _parser;
			CommonTokenStream _tokens;
			Method _compilationUnitMethod;
			AttributedString _string;

			@Override
			public AttributedString decorate(AttributedString string) {
				try {
					if (_string == null) {
						_string = string.copy();
						ANTLRInputStream inputStream = new ANTLRInputStream(_string.toString());

						_lexer = lexerFactory.create(inputStream);
						_tokens = new CommonTokenStream(_lexer);
						_parser = parserFactory.create(_tokens);

						_lexer.removeErrorListeners();
						_parser.removeErrorListeners();

						_compilationUnitMethod = _parser.getClass().getDeclaredMethod(rootRuleName);
						ParserRuleContext tree = (ParserRuleContext) _compilationUnitMethod.invoke(_parser);

						ParseTreeWalker walker = new ParseTreeWalker();
						ParseTreeListener extractor = visitorFactory.create(_lexer, _parser, _tokens, _string);
						walker.walk(extractor, tree);
					}
					return _string.copy();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void setNeedsRedraw() {
				_string = null;
			}
		};
	}
}
