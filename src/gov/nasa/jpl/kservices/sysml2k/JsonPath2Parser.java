// Generated from JsonPath2.g4 by ANTLR 4.5.3
package gov.nasa.jpl.kservices.sysml2k;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JsonPath2Parser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, STRING=7, FILTERSTART=8, 
		FILTEREND=9, WILDCARD=10, ROOTTAG=11, REFTAG=12, LIBTAG=13, IDENTIFIER=14, 
		NATURALNUM=15, WS=16;
	public static final int
		RULE_path = 0, RULE_element = 1, RULE_tagelement = 2, RULE_tag = 3, RULE_indexelement = 4, 
		RULE_index = 5, RULE_attributefilterelement = 6, RULE_attributevalue = 7, 
		RULE_doubleattributefilterelement = 8, RULE_negationfilterelement = 9, 
		RULE_alternationelement = 10, RULE_branches = 11;
	public static final String[] ruleNames = {
		"path", "element", "tagelement", "tag", "indexelement", "index", "attributefilterelement", 
		"attributevalue", "doubleattributefilterelement", "negationfilterelement", 
		"alternationelement", "branches"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.'", "'['", "']'", "'='", "'!'", "','", null, "'?('", "')'", "'*'", 
		"'$'", "'^'", "'LIB:'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "STRING", "FILTERSTART", "FILTEREND", 
		"WILDCARD", "ROOTTAG", "REFTAG", "LIBTAG", "IDENTIFIER", "NATURALNUM", 
		"WS"
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

	@Override
	public String getGrammarFileName() { return "JsonPath2.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JsonPath2Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class PathContext extends ParserRuleContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public BranchesContext branches() {
			return getRuleContext(BranchesContext.class,0);
		}
		public PathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathContext path() throws RecognitionException {
		PathContext _localctx = new PathContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24);
			element();
			setState(26);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << FILTERSTART) | (1L << ROOTTAG) | (1L << REFTAG) | (1L << LIBTAG))) != 0)) {
				{
				setState(25);
				branches();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementContext extends ParserRuleContext {
		public TagelementContext tagelement() {
			return getRuleContext(TagelementContext.class,0);
		}
		public IndexelementContext indexelement() {
			return getRuleContext(IndexelementContext.class,0);
		}
		public AttributefilterelementContext attributefilterelement() {
			return getRuleContext(AttributefilterelementContext.class,0);
		}
		public DoubleattributefilterelementContext doubleattributefilterelement() {
			return getRuleContext(DoubleattributefilterelementContext.class,0);
		}
		public NegationfilterelementContext negationfilterelement() {
			return getRuleContext(NegationfilterelementContext.class,0);
		}
		public AlternationelementContext alternationelement() {
			return getRuleContext(AlternationelementContext.class,0);
		}
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_element);
		try {
			setState(34);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				tagelement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(29);
				indexelement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(30);
				attributefilterelement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(31);
				doubleattributefilterelement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(32);
				negationfilterelement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(33);
				alternationelement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TagelementContext extends ParserRuleContext {
		public TerminalNode ROOTTAG() { return getToken(JsonPath2Parser.ROOTTAG, 0); }
		public TerminalNode REFTAG() { return getToken(JsonPath2Parser.REFTAG, 0); }
		public TerminalNode LIBTAG() { return getToken(JsonPath2Parser.LIBTAG, 0); }
		public TagContext tag() {
			return getRuleContext(TagContext.class,0);
		}
		public TagelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagelement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitTagelement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagelementContext tagelement() throws RecognitionException {
		TagelementContext _localctx = new TagelementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_tagelement);
		try {
			setState(41);
			switch (_input.LA(1)) {
			case ROOTTAG:
				enterOuterAlt(_localctx, 1);
				{
				setState(36);
				match(ROOTTAG);
				}
				break;
			case REFTAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(37);
				match(REFTAG);
				}
				break;
			case LIBTAG:
				enterOuterAlt(_localctx, 3);
				{
				setState(38);
				match(LIBTAG);
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 4);
				{
				setState(39);
				match(T__0);
				setState(40);
				tag();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TagContext extends ParserRuleContext {
		public TerminalNode WILDCARD() { return getToken(JsonPath2Parser.WILDCARD, 0); }
		public TerminalNode IDENTIFIER() { return getToken(JsonPath2Parser.IDENTIFIER, 0); }
		public TagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagContext tag() throws RecognitionException {
		TagContext _localctx = new TagContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43);
			_la = _input.LA(1);
			if ( !(_la==WILDCARD || _la==IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexelementContext extends ParserRuleContext {
		public IndexContext index() {
			return getRuleContext(IndexContext.class,0);
		}
		public IndexelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexelement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitIndexelement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexelementContext indexelement() throws RecognitionException {
		IndexelementContext _localctx = new IndexelementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_indexelement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			match(T__1);
			setState(46);
			index();
			setState(47);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexContext extends ParserRuleContext {
		public TerminalNode WILDCARD() { return getToken(JsonPath2Parser.WILDCARD, 0); }
		public TerminalNode NATURALNUM() { return getToken(JsonPath2Parser.NATURALNUM, 0); }
		public IndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitIndex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexContext index() throws RecognitionException {
		IndexContext _localctx = new IndexContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_index);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			_la = _input.LA(1);
			if ( !(_la==WILDCARD || _la==NATURALNUM) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributefilterelementContext extends ParserRuleContext {
		public TerminalNode FILTERSTART() { return getToken(JsonPath2Parser.FILTERSTART, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public AttributevalueContext attributevalue() {
			return getRuleContext(AttributevalueContext.class,0);
		}
		public TerminalNode FILTEREND() { return getToken(JsonPath2Parser.FILTEREND, 0); }
		public AttributefilterelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributefilterelement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitAttributefilterelement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributefilterelementContext attributefilterelement() throws RecognitionException {
		AttributefilterelementContext _localctx = new AttributefilterelementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_attributefilterelement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(FILTERSTART);
			setState(52);
			path();
			setState(53);
			match(T__3);
			setState(54);
			attributevalue();
			setState(55);
			match(FILTEREND);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributevalueContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(JsonPath2Parser.STRING, 0); }
		public AttributevalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributevalue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitAttributevalue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributevalueContext attributevalue() throws RecognitionException {
		AttributevalueContext _localctx = new AttributevalueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_attributevalue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DoubleattributefilterelementContext extends ParserRuleContext {
		public TerminalNode FILTERSTART() { return getToken(JsonPath2Parser.FILTERSTART, 0); }
		public List<PathContext> path() {
			return getRuleContexts(PathContext.class);
		}
		public PathContext path(int i) {
			return getRuleContext(PathContext.class,i);
		}
		public TerminalNode FILTEREND() { return getToken(JsonPath2Parser.FILTEREND, 0); }
		public DoubleattributefilterelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_doubleattributefilterelement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitDoubleattributefilterelement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DoubleattributefilterelementContext doubleattributefilterelement() throws RecognitionException {
		DoubleattributefilterelementContext _localctx = new DoubleattributefilterelementContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_doubleattributefilterelement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			match(FILTERSTART);
			setState(60);
			path();
			setState(61);
			match(T__3);
			setState(62);
			path();
			setState(63);
			match(FILTEREND);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NegationfilterelementContext extends ParserRuleContext {
		public TerminalNode FILTERSTART() { return getToken(JsonPath2Parser.FILTERSTART, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public TerminalNode FILTEREND() { return getToken(JsonPath2Parser.FILTEREND, 0); }
		public NegationfilterelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negationfilterelement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitNegationfilterelement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NegationfilterelementContext negationfilterelement() throws RecognitionException {
		NegationfilterelementContext _localctx = new NegationfilterelementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_negationfilterelement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(FILTERSTART);
			setState(66);
			match(T__4);
			setState(67);
			path();
			setState(68);
			match(FILTEREND);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlternationelementContext extends ParserRuleContext {
		public List<ElementContext> element() {
			return getRuleContexts(ElementContext.class);
		}
		public ElementContext element(int i) {
			return getRuleContext(ElementContext.class,i);
		}
		public AlternationelementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alternationelement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitAlternationelement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlternationelementContext alternationelement() throws RecognitionException {
		AlternationelementContext _localctx = new AlternationelementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_alternationelement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			match(T__1);
			setState(71);
			element();
			setState(76);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(72);
				match(T__5);
				setState(73);
				element();
				}
				}
				setState(78);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(79);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BranchesContext extends ParserRuleContext {
		public List<PathContext> path() {
			return getRuleContexts(PathContext.class);
		}
		public PathContext path(int i) {
			return getRuleContext(PathContext.class,i);
		}
		public BranchesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_branches; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JsonPath2Visitor ) return ((JsonPath2Visitor<? extends T>)visitor).visitBranches(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BranchesContext branches() throws RecognitionException {
		BranchesContext _localctx = new BranchesContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_branches);
		int _la;
		try {
			setState(93);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(81);
				path();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(82);
				match(T__1);
				setState(83);
				path();
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(84);
					match(T__5);
					setState(85);
					path();
					}
					}
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(91);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\22b\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\3\2\3\2\5\2\35\n\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3%\n\3\3\4"+
		"\3\4\3\4\3\4\3\4\5\4,\n\4\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\f\3\f\3\f\3\f\7\fM\n\f\f\f\16\fP\13\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\7"+
		"\rY\n\r\f\r\16\r\\\13\r\3\r\3\r\5\r`\n\r\3\r\2\2\16\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\2\4\4\2\f\f\20\20\4\2\f\f\21\21a\2\32\3\2\2\2\4$\3\2\2\2"+
		"\6+\3\2\2\2\b-\3\2\2\2\n/\3\2\2\2\f\63\3\2\2\2\16\65\3\2\2\2\20;\3\2\2"+
		"\2\22=\3\2\2\2\24C\3\2\2\2\26H\3\2\2\2\30_\3\2\2\2\32\34\5\4\3\2\33\35"+
		"\5\30\r\2\34\33\3\2\2\2\34\35\3\2\2\2\35\3\3\2\2\2\36%\5\6\4\2\37%\5\n"+
		"\6\2 %\5\16\b\2!%\5\22\n\2\"%\5\24\13\2#%\5\26\f\2$\36\3\2\2\2$\37\3\2"+
		"\2\2$ \3\2\2\2$!\3\2\2\2$\"\3\2\2\2$#\3\2\2\2%\5\3\2\2\2&,\7\r\2\2\',"+
		"\7\16\2\2(,\7\17\2\2)*\7\3\2\2*,\5\b\5\2+&\3\2\2\2+\'\3\2\2\2+(\3\2\2"+
		"\2+)\3\2\2\2,\7\3\2\2\2-.\t\2\2\2.\t\3\2\2\2/\60\7\4\2\2\60\61\5\f\7\2"+
		"\61\62\7\5\2\2\62\13\3\2\2\2\63\64\t\3\2\2\64\r\3\2\2\2\65\66\7\n\2\2"+
		"\66\67\5\2\2\2\678\7\6\2\289\5\20\t\29:\7\13\2\2:\17\3\2\2\2;<\7\t\2\2"+
		"<\21\3\2\2\2=>\7\n\2\2>?\5\2\2\2?@\7\6\2\2@A\5\2\2\2AB\7\13\2\2B\23\3"+
		"\2\2\2CD\7\n\2\2DE\7\7\2\2EF\5\2\2\2FG\7\13\2\2G\25\3\2\2\2HI\7\4\2\2"+
		"IN\5\4\3\2JK\7\b\2\2KM\5\4\3\2LJ\3\2\2\2MP\3\2\2\2NL\3\2\2\2NO\3\2\2\2"+
		"OQ\3\2\2\2PN\3\2\2\2QR\7\5\2\2R\27\3\2\2\2S`\5\2\2\2TU\7\4\2\2UZ\5\2\2"+
		"\2VW\7\b\2\2WY\5\2\2\2XV\3\2\2\2Y\\\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[]\3\2"+
		"\2\2\\Z\3\2\2\2]^\7\5\2\2^`\3\2\2\2_S\3\2\2\2_T\3\2\2\2`\31\3\2\2\2\b"+
		"\34$+NZ_";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}