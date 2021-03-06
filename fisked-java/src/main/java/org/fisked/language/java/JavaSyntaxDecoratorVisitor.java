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
package org.fisked.language.java;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaSyntaxDecoratorVisitor extends Java8BaseListener implements ANTLRErrorListener {
	private final AttributedString _string;
	private final CommonTokenStream _tokens;

	boolean _isReadingReferenceType = false;
	boolean _isReadingPrimitiveType = false;
	boolean _isReadingVariable = false;
	boolean _isReadingLiteral = false;
	boolean _isReadingAnnotation = false;
	boolean _isReadingModifier = false;

	public JavaSyntaxDecoratorVisitor(Java8Lexer lexer, Java8Parser parser, CommonTokenStream tokens,
			AttributedString string) {
		_string = string;
		lexer.addErrorListener(this);
		parser.addErrorListener(this);
		_tokens = tokens;
	}

	@Override
	public void enterEveryRule(ParserRuleContext ruleContext) {
	}

	@Override
	public void exitEveryRule(ParserRuleContext ruleContext) {

	}

	private final static Logger LOG = LoggerFactory.getLogger(JavaSyntaxDecoratorVisitor.class);

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		Token symbol = (Token) offendingSymbol;

		int startIndex = symbol.getStartIndex();
		int endIndex = symbol.getStopIndex();

		LOG.debug("Indices for error: {" + startIndex + ", " + endIndex + "}");

		_string.setBackgroundColor(Color.RED, startIndex, endIndex);
		_string.setBold(startIndex, endIndex);
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
			BitSet ambigAlts, ATNConfigSet configs) {

	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
			BitSet conflictingAlts, ATNConfigSet configs) {

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
			ATNConfigSet configs) {

	}

	@Override
	public void visitErrorNode(ErrorNode error) {
		int startIndex = error.getSourceInterval().a;
		int endIndex = error.getSourceInterval().b;

		LOG.debug("Token indices for error: {" + startIndex + ", " + endIndex + "}");

		if (startIndex == -1)
			return;

		Token startToken = _tokens.get(startIndex);
		Token stopToken = _tokens.get(endIndex);

		startIndex = startToken.getStartIndex();
		endIndex = stopToken.getStopIndex() + 1;

		LOG.debug("Indices for error: {" + startIndex + ", " + endIndex + "}");

		_string.setBackgroundColor(Color.RED, startIndex, endIndex);
		_string.setBold(startIndex, endIndex);
	}

	@Override
	public void visitTerminal(TerminalNode terminal) {
		final Token symbol = terminal.getSymbol();
		final int termType = symbol.getType();
		final int startIndex = symbol.getStartIndex();
		final int endIndex = symbol.getStopIndex() + 1;

		if (_isReadingVariable) {
			_string.setForegroundColor(Color.BLUE, startIndex, endIndex);
		} else if (_isReadingModifier) {
			_string.setForegroundColor(Color.GREEN, startIndex, endIndex);
		} else if (_isReadingPrimitiveType) {
			_string.setForegroundColor(Color.MAGENTA, startIndex, endIndex);
		} else if (_isReadingReferenceType && termType == Java8Lexer.Identifier) {
			_string.setForegroundColor(Color.RED, startIndex, endIndex);
		} else if (_isReadingLiteral) {
			_string.setForegroundColor(Color.CYAN, startIndex, endIndex);
		} else if (_isReadingAnnotation) {
			_string.setForegroundColor(Color.RED, startIndex, endIndex);
		} else if (termType >= Java8Lexer.ABSTRACT && termType <= Java8Lexer.WHILE) {
			_string.setForegroundColor(Color.YELLOW, startIndex, endIndex);
		} else if (termType == Java8Lexer.Identifier) {
			_string.setForegroundColor(Color.BLUE, startIndex, endIndex);
		} else if (termType == Java8Lexer.COMMENT) {
			_string.setForegroundColor(Color.WHITE, startIndex, endIndex);
		}
	}

	@Override
	public void enterTypeIdentifier(Java8Parser.TypeIdentifierContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitTypeIdentifier(Java8Parser.TypeIdentifierContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterConstantModifier(Java8Parser.ConstantModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitConstantModifier(Java8Parser.ConstantModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterArrayType(Java8Parser.ArrayTypeContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitArrayType(Java8Parser.ArrayTypeContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterSimpleTypeName(Java8Parser.SimpleTypeNameContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitSimpleTypeName(Java8Parser.SimpleTypeNameContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterVariableDeclaratorId(Java8Parser.VariableDeclaratorIdContext ctx) {
		_isReadingVariable = true;
	}

	@Override
	public void exitVariableDeclaratorId(Java8Parser.VariableDeclaratorIdContext ctx) {
		_isReadingVariable = false;
	}

	@Override
	public void enterExceptionType(Java8Parser.ExceptionTypeContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitExceptionType(Java8Parser.ExceptionTypeContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterReferenceType(Java8Parser.ReferenceTypeContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitReferenceType(Java8Parser.ReferenceTypeContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterLiteral(Java8Parser.LiteralContext ctx) {
		_isReadingLiteral = true;
	}

	@Override
	public void exitLiteral(Java8Parser.LiteralContext ctx) {
		_isReadingLiteral = false;
	}

	@Override
	public void enterAnnotation(Java8Parser.AnnotationContext ctx) {
		_isReadingAnnotation = true;
	}

	@Override
	public void exitAnnotation(Java8Parser.AnnotationContext ctx) {
		_isReadingAnnotation = false;
	}

	@Override
	public void enterPackageModifier(Java8Parser.PackageModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitPackageModifier(Java8Parser.PackageModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterConstructorModifier(Java8Parser.ConstructorModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitConstructorModifier(Java8Parser.ConstructorModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterMethodModifier(Java8Parser.MethodModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitMethodModifier(Java8Parser.MethodModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterUnannClassType(Java8Parser.UnannClassTypeContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitUnannClassType(Java8Parser.UnannClassTypeContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterTypeVariable(Java8Parser.TypeVariableContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitTypeVariable(Java8Parser.TypeVariableContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterEnumConstantModifier(Java8Parser.EnumConstantModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitEnumConstantModifier(Java8Parser.EnumConstantModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterFieldModifier(Java8Parser.FieldModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitFieldModifier(Java8Parser.FieldModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterAnnotationTypeElementModifier(Java8Parser.AnnotationTypeElementModifierContext ctx) {
		_isReadingAnnotation = true;
	}

	@Override
	public void exitAnnotationTypeElementModifier(Java8Parser.AnnotationTypeElementModifierContext ctx) {
		_isReadingAnnotation = false;
	}

	@Override
	public void enterClassModifier(Java8Parser.ClassModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitClassModifier(Java8Parser.ClassModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterVariableModifier(Java8Parser.VariableModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitVariableModifier(Java8Parser.VariableModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterTypeParameterModifier(Java8Parser.TypeParameterModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitTypeParameterModifier(Java8Parser.TypeParameterModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterInterfaceMethodModifier(Java8Parser.InterfaceMethodModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitInterfaceMethodModifier(Java8Parser.InterfaceMethodModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterInterfaceModifier(Java8Parser.InterfaceModifierContext ctx) {
		_isReadingModifier = true;
	}

	@Override
	public void exitInterfaceModifier(Java8Parser.InterfaceModifierContext ctx) {
		_isReadingModifier = false;
	}

	@Override
	public void enterUnannReferenceType(Java8Parser.UnannReferenceTypeContext ctx) {
		_isReadingReferenceType = true;
	}

	@Override
	public void exitUnannReferenceType(Java8Parser.UnannReferenceTypeContext ctx) {
		_isReadingReferenceType = false;
	}

	@Override
	public void enterPrimitiveType(Java8Parser.PrimitiveTypeContext ctx) {
		_isReadingPrimitiveType = true;
	}

	@Override
	public void exitPrimitiveType(Java8Parser.PrimitiveTypeContext ctx) {
		_isReadingPrimitiveType = false;
	}

}
