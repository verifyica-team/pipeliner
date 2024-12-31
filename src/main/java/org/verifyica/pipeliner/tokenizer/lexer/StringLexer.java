// Generated from /home/user/Development/github/verifyica-team/pipeliner/src/antlr4/String.grammar by ANTLR 4.13.2
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
public class StringLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PROPERTY=1, ENVIRONMENT_VARIABLE=2, ENVIRONMENT_VARIABLE_WITH_BRACES=3, 
		TEXT=4, NEWLINE=5, WS=6;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PROPERTY", "ENVIRONMENT_VARIABLE", "ENVIRONMENT_VARIABLE_WITH_BRACES", 
			"TEXT", "NEWLINE", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PROPERTY", "ENVIRONMENT_VARIABLE", "ENVIRONMENT_VARIABLE_WITH_BRACES", 
			"TEXT", "NEWLINE", "WS"
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


	public StringLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "String.grammar"; }

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
		"\u0004\u0000\u0006R\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0005\u0000\u0013\b\u0000\n\u0000\f\u0000\u0016"+
		"\t\u0000\u0001\u0000\u0001\u0000\u0005\u0000\u001a\b\u0000\n\u0000\f\u0000"+
		"\u001d\t\u0000\u0001\u0000\u0005\u0000 \b\u0000\n\u0000\f\u0000#\t\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0005\u0001+\b\u0001\n\u0001\f\u0001.\t\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u00025\b\u0002\n\u0002\f\u0002"+
		"8\t\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0004\u0003=\b\u0003\u000b"+
		"\u0003\f\u0003>\u0001\u0003\u0001\u0003\u0004\u0003C\b\u0003\u000b\u0003"+
		"\f\u0003D\u0001\u0004\u0004\u0004H\b\u0004\u000b\u0004\f\u0004I\u0001"+
		"\u0005\u0004\u0005M\b\u0005\u000b\u0005\f\u0005N\u0001\u0005\u0001\u0005"+
		"\u0000\u0000\u0006\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005"+
		"\u000b\u0006\u0001\u0000\u0007\u0002\u0000\t\t  \u0003\u0000AZ__az\u0005"+
		"\u0000-.09AZ__az\u0004\u000009AZ__az\u0005\u0000--09AZ__az\u0004\u0000"+
		"\n\n$$\\\\{{\u0002\u0000\n\n\r\r[\u0000\u0001\u0001\u0000\u0000\u0000"+
		"\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000"+
		"\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000"+
		"\u000b\u0001\u0000\u0000\u0000\u0001\r\u0001\u0000\u0000\u0000\u0003\'"+
		"\u0001\u0000\u0000\u0000\u0005/\u0001\u0000\u0000\u0000\u0007B\u0001\u0000"+
		"\u0000\u0000\tG\u0001\u0000\u0000\u0000\u000bL\u0001\u0000\u0000\u0000"+
		"\r\u000e\u0005$\u0000\u0000\u000e\u000f\u0005{\u0000\u0000\u000f\u0010"+
		"\u0005{\u0000\u0000\u0010\u0014\u0001\u0000\u0000\u0000\u0011\u0013\u0007"+
		"\u0000\u0000\u0000\u0012\u0011\u0001\u0000\u0000\u0000\u0013\u0016\u0001"+
		"\u0000\u0000\u0000\u0014\u0012\u0001\u0000\u0000\u0000\u0014\u0015\u0001"+
		"\u0000\u0000\u0000\u0015\u0017\u0001\u0000\u0000\u0000\u0016\u0014\u0001"+
		"\u0000\u0000\u0000\u0017\u001b\u0007\u0001\u0000\u0000\u0018\u001a\u0007"+
		"\u0002\u0000\u0000\u0019\u0018\u0001\u0000\u0000\u0000\u001a\u001d\u0001"+
		"\u0000\u0000\u0000\u001b\u0019\u0001\u0000\u0000\u0000\u001b\u001c\u0001"+
		"\u0000\u0000\u0000\u001c!\u0001\u0000\u0000\u0000\u001d\u001b\u0001\u0000"+
		"\u0000\u0000\u001e \u0007\u0000\u0000\u0000\u001f\u001e\u0001\u0000\u0000"+
		"\u0000 #\u0001\u0000\u0000\u0000!\u001f\u0001\u0000\u0000\u0000!\"\u0001"+
		"\u0000\u0000\u0000\"$\u0001\u0000\u0000\u0000#!\u0001\u0000\u0000\u0000"+
		"$%\u0005}\u0000\u0000%&\u0005}\u0000\u0000&\u0002\u0001\u0000\u0000\u0000"+
		"\'(\u0005$\u0000\u0000(,\u0007\u0001\u0000\u0000)+\u0007\u0003\u0000\u0000"+
		"*)\u0001\u0000\u0000\u0000+.\u0001\u0000\u0000\u0000,*\u0001\u0000\u0000"+
		"\u0000,-\u0001\u0000\u0000\u0000-\u0004\u0001\u0000\u0000\u0000.,\u0001"+
		"\u0000\u0000\u0000/0\u0005$\u0000\u000001\u0005{\u0000\u000012\u0001\u0000"+
		"\u0000\u000026\u0007\u0001\u0000\u000035\u0007\u0004\u0000\u000043\u0001"+
		"\u0000\u0000\u000058\u0001\u0000\u0000\u000064\u0001\u0000\u0000\u0000"+
		"67\u0001\u0000\u0000\u000079\u0001\u0000\u0000\u000086\u0001\u0000\u0000"+
		"\u00009:\u0005}\u0000\u0000:\u0006\u0001\u0000\u0000\u0000;=\b\u0005\u0000"+
		"\u0000<;\u0001\u0000\u0000\u0000=>\u0001\u0000\u0000\u0000><\u0001\u0000"+
		"\u0000\u0000>?\u0001\u0000\u0000\u0000?C\u0001\u0000\u0000\u0000@A\u0005"+
		"\\\u0000\u0000AC\u0005$\u0000\u0000B<\u0001\u0000\u0000\u0000B@\u0001"+
		"\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000"+
		"DE\u0001\u0000\u0000\u0000E\b\u0001\u0000\u0000\u0000FH\u0007\u0006\u0000"+
		"\u0000GF\u0001\u0000\u0000\u0000HI\u0001\u0000\u0000\u0000IG\u0001\u0000"+
		"\u0000\u0000IJ\u0001\u0000\u0000\u0000J\n\u0001\u0000\u0000\u0000KM\u0007"+
		"\u0000\u0000\u0000LK\u0001\u0000\u0000\u0000MN\u0001\u0000\u0000\u0000"+
		"NL\u0001\u0000\u0000\u0000NO\u0001\u0000\u0000\u0000OP\u0001\u0000\u0000"+
		"\u0000PQ\u0006\u0005\u0000\u0000Q\f\u0001\u0000\u0000\u0000\u000b\u0000"+
		"\u0014\u001b!,6>BDIN\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}