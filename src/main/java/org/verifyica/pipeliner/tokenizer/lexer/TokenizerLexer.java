// Generated from Tokenizer.g4 by ANTLR 4.13.2
package org.verifyica.pipeliner.tokenizer.lexer;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TokenizerLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PROPERTY=1, ENVIRONMENT_VARIABLE=2, ENVIRONMENT_VARIABLE_WITH_BRACES=3, 
		ESCAPED_DOLLAR=4, BACKSLASH=5, DOLLAR=6, TEXT=7, NEWLINE=8, WS=9;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PROPERTY", "ENVIRONMENT_VARIABLE", "ENVIRONMENT_VARIABLE_WITH_BRACES", 
			"ESCAPED_DOLLAR", "BACKSLASH", "DOLLAR", "TEXT", "NEWLINE", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, "'\\'", "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PROPERTY", "ENVIRONMENT_VARIABLE", "ENVIRONMENT_VARIABLE_WITH_BRACES", 
			"ESCAPED_DOLLAR", "BACKSLASH", "DOLLAR", "TEXT", "NEWLINE", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public TokenizerLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Tokenizer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\tg\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0005\u0000\u0019\b\u0000\n\u0000\f\u0000\u001c\t\u0000"+
		"\u0001\u0000\u0005\u0000\u001f\b\u0000\n\u0000\f\u0000\"\t\u0000\u0001"+
		"\u0000\u0001\u0000\u0005\u0000&\b\u0000\n\u0000\f\u0000)\t\u0000\u0001"+
		"\u0000\u0005\u0000,\b\u0000\n\u0000\f\u0000/\t\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u00017\b"+
		"\u0001\n\u0001\f\u0001:\t\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0005\u0002A\b\u0002\n\u0002\f\u0002D\t\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003Q\b"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0004"+
		"\u0006X\b\u0006\u000b\u0006\f\u0006Y\u0001\u0007\u0004\u0007]\b\u0007"+
		"\u000b\u0007\f\u0007^\u0001\b\u0004\bb\b\b\u000b\b\f\bc\u0001\b\u0001"+
		"\b\u0000\u0000\t\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005"+
		"\u000b\u0006\r\u0007\u000f\b\u0011\t\u0001\u0000\b\u0002\u0000\\\\}}\u0002"+
		"\u0000\t\t  \u0003\u0000AZ__az\u0005\u0000-.09AZ__az\u0004\u000009AZ_"+
		"_az\u0005\u0000--09AZ__az\u0004\u0000\n\n$$\\\\{{\u0002\u0000\n\n\r\r"+
		"q\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000"+
		"\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000"+
		"\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000"+
		"\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011"+
		"\u0001\u0000\u0000\u0000\u0001\u0013\u0001\u0000\u0000\u0000\u00033\u0001"+
		"\u0000\u0000\u0000\u0005;\u0001\u0000\u0000\u0000\u0007P\u0001\u0000\u0000"+
		"\u0000\tR\u0001\u0000\u0000\u0000\u000bT\u0001\u0000\u0000\u0000\rW\u0001"+
		"\u0000\u0000\u0000\u000f\\\u0001\u0000\u0000\u0000\u0011a\u0001\u0000"+
		"\u0000\u0000\u0013\u0014\u0005$\u0000\u0000\u0014\u0015\u0005{\u0000\u0000"+
		"\u0015\u0016\u0005{\u0000\u0000\u0016\u001a\u0001\u0000\u0000\u0000\u0017"+
		"\u0019\b\u0000\u0000\u0000\u0018\u0017\u0001\u0000\u0000\u0000\u0019\u001c"+
		"\u0001\u0000\u0000\u0000\u001a\u0018\u0001\u0000\u0000\u0000\u001a\u001b"+
		"\u0001\u0000\u0000\u0000\u001b \u0001\u0000\u0000\u0000\u001c\u001a\u0001"+
		"\u0000\u0000\u0000\u001d\u001f\u0007\u0001\u0000\u0000\u001e\u001d\u0001"+
		"\u0000\u0000\u0000\u001f\"\u0001\u0000\u0000\u0000 \u001e\u0001\u0000"+
		"\u0000\u0000 !\u0001\u0000\u0000\u0000!#\u0001\u0000\u0000\u0000\" \u0001"+
		"\u0000\u0000\u0000#\'\u0007\u0002\u0000\u0000$&\u0007\u0003\u0000\u0000"+
		"%$\u0001\u0000\u0000\u0000&)\u0001\u0000\u0000\u0000\'%\u0001\u0000\u0000"+
		"\u0000\'(\u0001\u0000\u0000\u0000(-\u0001\u0000\u0000\u0000)\'\u0001\u0000"+
		"\u0000\u0000*,\u0007\u0001\u0000\u0000+*\u0001\u0000\u0000\u0000,/\u0001"+
		"\u0000\u0000\u0000-+\u0001\u0000\u0000\u0000-.\u0001\u0000\u0000\u0000"+
		".0\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000\u000001\u0005}\u0000\u0000"+
		"12\u0005}\u0000\u00002\u0002\u0001\u0000\u0000\u000034\u0005$\u0000\u0000"+
		"48\u0007\u0002\u0000\u000057\u0007\u0004\u0000\u000065\u0001\u0000\u0000"+
		"\u00007:\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u000089\u0001\u0000"+
		"\u0000\u00009\u0004\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000"+
		";<\u0005$\u0000\u0000<=\u0005{\u0000\u0000=>\u0001\u0000\u0000\u0000>"+
		"B\u0007\u0002\u0000\u0000?A\u0007\u0005\u0000\u0000@?\u0001\u0000\u0000"+
		"\u0000AD\u0001\u0000\u0000\u0000B@\u0001\u0000\u0000\u0000BC\u0001\u0000"+
		"\u0000\u0000CE\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000EF\u0005"+
		"}\u0000\u0000F\u0006\u0001\u0000\u0000\u0000GH\u0005\\\u0000\u0000HI\u0005"+
		"$\u0000\u0000IJ\u0005{\u0000\u0000JQ\u0005{\u0000\u0000KL\u0005\\\u0000"+
		"\u0000LM\u0005$\u0000\u0000MQ\u0005{\u0000\u0000NO\u0005\\\u0000\u0000"+
		"OQ\u0005$\u0000\u0000PG\u0001\u0000\u0000\u0000PK\u0001\u0000\u0000\u0000"+
		"PN\u0001\u0000\u0000\u0000Q\b\u0001\u0000\u0000\u0000RS\u0005\\\u0000"+
		"\u0000S\n\u0001\u0000\u0000\u0000TU\u0005$\u0000\u0000U\f\u0001\u0000"+
		"\u0000\u0000VX\b\u0006\u0000\u0000WV\u0001\u0000\u0000\u0000XY\u0001\u0000"+
		"\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000Z\u000e"+
		"\u0001\u0000\u0000\u0000[]\u0007\u0007\u0000\u0000\\[\u0001\u0000\u0000"+
		"\u0000]^\u0001\u0000\u0000\u0000^\\\u0001\u0000\u0000\u0000^_\u0001\u0000"+
		"\u0000\u0000_\u0010\u0001\u0000\u0000\u0000`b\u0007\u0001\u0000\u0000"+
		"a`\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000\u0000ca\u0001\u0000\u0000"+
		"\u0000cd\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000ef\u0006\b\u0000"+
		"\u0000f\u0012\u0001\u0000\u0000\u0000\u000b\u0000\u001a \'-8BPY^c\u0001"+
		"\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}