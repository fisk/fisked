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
import org.fisked.text.IBufferDecorator;
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
