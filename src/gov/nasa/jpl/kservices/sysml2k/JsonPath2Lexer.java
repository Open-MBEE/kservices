// Generated from JsonPath2.g4 by ANTLR 4.5.3
package gov.nasa.jpl.kservices.sysml2k;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JsonPath2Lexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, STRING=7, FILTERSTART=8, 
		FILTEREND=9, WILDCARD=10, ROOTTAG=11, REFTAG=12, IDENTIFIER=13, NATURALNUM=14, 
		WS=15;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "STRING", "FILTERSTART", 
		"FILTEREND", "WILDCARD", "ROOTTAG", "REFTAG", "IDENTIFIER", "NATURALNUM", 
		"WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'['", "']'", "'='", "'!'", "','", null, "'?('", "')'", "'*'", 
		"'$'", "'^'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "STRING", "FILTERSTART", "FILTEREND", 
		"WILDCARD", "ROOTTAG", "REFTAG", "IDENTIFIER", "NATURALNUM", "WS"
	};
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


	public JsonPath2Lexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JsonPath2.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\21[\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\7\b\62\n\b\f\b\16\b\65\13"+
		"\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\7\16"+
		"F\n\16\f\16\16\16I\13\16\3\17\3\17\3\17\7\17N\n\17\f\17\16\17Q\13\17\5"+
		"\17S\n\17\3\20\6\20V\n\20\r\20\16\20W\3\20\3\20\2\2\21\3\3\5\4\7\5\t\6"+
		"\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21\3\2\b\4\2"+
		"$$^^\5\2C\\aac|\6\2\62;C\\aac|\3\2\63;\3\2\62;\5\2\13\f\17\17\"\"`\2\3"+
		"\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2"+
		"\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\3!\3\2\2\2\5#\3\2\2\2"+
		"\7%\3\2\2\2\t\'\3\2\2\2\13)\3\2\2\2\r+\3\2\2\2\17-\3\2\2\2\218\3\2\2\2"+
		"\23;\3\2\2\2\25=\3\2\2\2\27?\3\2\2\2\31A\3\2\2\2\33C\3\2\2\2\35R\3\2\2"+
		"\2\37U\3\2\2\2!\"\7\60\2\2\"\4\3\2\2\2#$\7]\2\2$\6\3\2\2\2%&\7_\2\2&\b"+
		"\3\2\2\2\'(\7?\2\2(\n\3\2\2\2)*\7#\2\2*\f\3\2\2\2+,\7.\2\2,\16\3\2\2\2"+
		"-\63\7$\2\2.\62\n\2\2\2/\60\7^\2\2\60\62\t\2\2\2\61.\3\2\2\2\61/\3\2\2"+
		"\2\62\65\3\2\2\2\63\61\3\2\2\2\63\64\3\2\2\2\64\66\3\2\2\2\65\63\3\2\2"+
		"\2\66\67\7$\2\2\67\20\3\2\2\289\7A\2\29:\7*\2\2:\22\3\2\2\2;<\7+\2\2<"+
		"\24\3\2\2\2=>\7,\2\2>\26\3\2\2\2?@\7&\2\2@\30\3\2\2\2AB\7`\2\2B\32\3\2"+
		"\2\2CG\t\3\2\2DF\t\4\2\2ED\3\2\2\2FI\3\2\2\2GE\3\2\2\2GH\3\2\2\2H\34\3"+
		"\2\2\2IG\3\2\2\2JS\7\62\2\2KO\t\5\2\2LN\t\6\2\2ML\3\2\2\2NQ\3\2\2\2OM"+
		"\3\2\2\2OP\3\2\2\2PS\3\2\2\2QO\3\2\2\2RJ\3\2\2\2RK\3\2\2\2S\36\3\2\2\2"+
		"TV\t\7\2\2UT\3\2\2\2VW\3\2\2\2WU\3\2\2\2WX\3\2\2\2XY\3\2\2\2YZ\b\20\2"+
		"\2Z \3\2\2\2\t\2\61\63GORW\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}