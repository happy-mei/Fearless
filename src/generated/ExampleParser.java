// Generated from java-escape by ANTLR 4.11.1
package generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ExampleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3;
	public static final int
		RULE_e = 0, RULE_mCall = 1, RULE_x = 2, RULE_lambda = 3, RULE_nudeE = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"e", "mCall", "x", "lambda", "nudeE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'MCall'", "'X'", "'Lambda'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
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

	@Override
	public String getGrammarFileName() { return "java-escape"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ExampleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EContext extends ParserRuleContext {
		public MCallContext mCall() {
			return getRuleContext(MCallContext.class,0);
		}
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public LambdaContext lambda() {
			return getRuleContext(LambdaContext.class,0);
		}
		public EContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_e; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).enterE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).exitE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExampleVisitor ) return ((ExampleVisitor<? extends T>)visitor).visitE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EContext e() throws RecognitionException {
		EContext _localctx = new EContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_e);
		try {
			setState(13);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(10);
				mCall();
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(11);
				x();
				}
				break;
			case T__2:
				enterOuterAlt(_localctx, 3);
				{
				setState(12);
				lambda();
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

	@SuppressWarnings("CheckReturnValue")
	public static class MCallContext extends ParserRuleContext {
		public MCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).enterMCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).exitMCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExampleVisitor ) return ((ExampleVisitor<? extends T>)visitor).visitMCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MCallContext mCall() throws RecognitionException {
		MCallContext _localctx = new MCallContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_mCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(15);
			match(T__0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class XContext extends ParserRuleContext {
		public XContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_x; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).enterX(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).exitX(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExampleVisitor ) return ((ExampleVisitor<? extends T>)visitor).visitX(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XContext x() throws RecognitionException {
		XContext _localctx = new XContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_x);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(17);
			match(T__1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaContext extends ParserRuleContext {
		public LambdaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambda; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExampleVisitor ) return ((ExampleVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaContext lambda() throws RecognitionException {
		LambdaContext _localctx = new LambdaContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_lambda);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19);
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

	@SuppressWarnings("CheckReturnValue")
	public static class NudeEContext extends ParserRuleContext {
		public EContext e() {
			return getRuleContext(EContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ExampleParser.EOF, 0); }
		public NudeEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).enterNudeE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExampleListener ) ((ExampleListener)listener).exitNudeE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExampleVisitor ) return ((ExampleVisitor<? extends T>)visitor).visitNudeE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeEContext nudeE() throws RecognitionException {
		NudeEContext _localctx = new NudeEContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_nudeE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			e();
			setState(22);
			match(EOF);
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
		"\u0004\u0001\u0003\u0019\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000\u000e\b\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0000\u0000\u0005\u0000\u0002\u0004"+
		"\u0006\b\u0000\u0000\u0015\u0000\r\u0001\u0000\u0000\u0000\u0002\u000f"+
		"\u0001\u0000\u0000\u0000\u0004\u0011\u0001\u0000\u0000\u0000\u0006\u0013"+
		"\u0001\u0000\u0000\u0000\b\u0015\u0001\u0000\u0000\u0000\n\u000e\u0003"+
		"\u0002\u0001\u0000\u000b\u000e\u0003\u0004\u0002\u0000\f\u000e\u0003\u0006"+
		"\u0003\u0000\r\n\u0001\u0000\u0000\u0000\r\u000b\u0001\u0000\u0000\u0000"+
		"\r\f\u0001\u0000\u0000\u0000\u000e\u0001\u0001\u0000\u0000\u0000\u000f"+
		"\u0010\u0005\u0001\u0000\u0000\u0010\u0003\u0001\u0000\u0000\u0000\u0011"+
		"\u0012\u0005\u0002\u0000\u0000\u0012\u0005\u0001\u0000\u0000\u0000\u0013"+
		"\u0014\u0005\u0003\u0000\u0000\u0014\u0007\u0001\u0000\u0000\u0000\u0015"+
		"\u0016\u0003\u0000\u0000\u0000\u0016\u0017\u0005\u0000\u0000\u0001\u0017"+
		"\t\u0001\u0000\u0000\u0000\u0001\r";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}