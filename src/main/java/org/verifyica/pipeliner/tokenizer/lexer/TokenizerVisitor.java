// Generated from Tokenizer.g4 by ANTLR 4.13.2
package org.verifyica.pipeliner.tokenizer.lexer;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TokenizerParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TokenizerVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(TokenizerParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(TokenizerParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#backslash}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackslash(TokenizerParser.BackslashContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#backslashDoubleQuote}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackslashDoubleQuote(TokenizerParser.BackslashDoubleQuoteContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#dollar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDollar(TokenizerParser.DollarContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#leftParenthesis}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftParenthesis(TokenizerParser.LeftParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#rightParenthesis}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRightParenthesis(TokenizerParser.RightParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#quote}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuote(TokenizerParser.QuoteContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#doubleQuote}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleQuote(TokenizerParser.DoubleQuoteContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(TokenizerParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by {@link TokenizerParser#line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLine(TokenizerParser.LineContext ctx);
}