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
public class FearlessParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Mut=1, Lent=2, Read=3, Iso=4, RecMdf=5, Mdf=6, Imm=7, Eq=8, Alias=9, As=10, 
		OC=11, CC=12, OS=13, CS=14, OR=15, CR=16, Comma=17, Colon=18, Arrow=19, 
		Underscore=20, X=21, MName=22, BlockComment=23, LineComment=24, SysInM=25, 
		FullCN=26, Whitespace=27, Pack=28;
	public static final int
		RULE_fullCN = 0, RULE_x = 1, RULE_m = 2, RULE_mdf = 3, RULE_roundE = 4, 
		RULE_mGen = 5, RULE_lambda = 6, RULE_block = 7, RULE_bblock = 8, RULE_t = 9, 
		RULE_singleM = 10, RULE_meth = 11, RULE_sig = 12, RULE_gamma = 13, RULE_topDec = 14, 
		RULE_alias = 15, RULE_atomE = 16, RULE_postE = 17, RULE_pOp = 18, RULE_e = 19, 
		RULE_nudeE = 20, RULE_nudeProgram = 21;
	private static String[] makeRuleNames() {
		return new String[] {
			"fullCN", "x", "m", "mdf", "roundE", "mGen", "lambda", "block", "bblock", 
			"t", "singleM", "meth", "sig", "gamma", "topDec", "alias", "atomE", "postE", 
			"pOp", "e", "nudeE", "nudeProgram"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'mut'", "'lent'", "'read'", "'iso'", "'recMdf'", "'mdf'", "'imm'", 
			"'='", "'alias'", "'as'", "'{'", "'}'", "'['", "']'", "'('", "')'", "','", 
			"':'", "'->'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Mut", "Lent", "Read", "Iso", "RecMdf", "Mdf", "Imm", "Eq", "Alias", 
			"As", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", "Arrow", 
			"Underscore", "X", "MName", "BlockComment", "LineComment", "SysInM", 
			"FullCN", "Whitespace", "Pack"
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

	public FearlessParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FullCNContext extends ParserRuleContext {
		public TerminalNode FullCN() { return getToken(FearlessParser.FullCN, 0); }
		public FullCNContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullCN; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterFullCN(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitFullCN(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitFullCN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullCNContext fullCN() throws RecognitionException {
		FullCNContext _localctx = new FullCNContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_fullCN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			match(FullCN);
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
		public TerminalNode X() { return getToken(FearlessParser.X, 0); }
		public TerminalNode Underscore() { return getToken(FearlessParser.Underscore, 0); }
		public XContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_x; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterX(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitX(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitX(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XContext x() throws RecognitionException {
		XContext _localctx = new XContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_x);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			_la = _input.LA(1);
			if ( !(_la==Underscore || _la==X) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MContext extends ParserRuleContext {
		public TerminalNode SysInM() { return getToken(FearlessParser.SysInM, 0); }
		public TerminalNode MName() { return getToken(FearlessParser.MName, 0); }
		public MContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_m; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterM(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitM(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitM(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MContext m() throws RecognitionException {
		MContext _localctx = new MContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_m);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			_la = _input.LA(1);
			if ( !(_la==MName || _la==SysInM) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MdfContext extends ParserRuleContext {
		public TerminalNode Mut() { return getToken(FearlessParser.Mut, 0); }
		public TerminalNode Lent() { return getToken(FearlessParser.Lent, 0); }
		public TerminalNode Read() { return getToken(FearlessParser.Read, 0); }
		public TerminalNode Iso() { return getToken(FearlessParser.Iso, 0); }
		public TerminalNode RecMdf() { return getToken(FearlessParser.RecMdf, 0); }
		public TerminalNode Mdf() { return getToken(FearlessParser.Mdf, 0); }
		public TerminalNode Imm() { return getToken(FearlessParser.Imm, 0); }
		public MdfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mdf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterMdf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitMdf(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitMdf(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MdfContext mdf() throws RecognitionException {
		MdfContext _localctx = new MdfContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_mdf);
		try {
			setState(58);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(51);
				match(Mut);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(52);
				match(Lent);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(53);
				match(Read);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(54);
				match(Iso);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(55);
				match(RecMdf);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(56);
				match(Mdf);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(57);
				match(Imm);
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

	@SuppressWarnings("CheckReturnValue")
	public static class RoundEContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(FearlessParser.OR, 0); }
		public EContext e() {
			return getRuleContext(EContext.class,0);
		}
		public TerminalNode CR() { return getToken(FearlessParser.CR, 0); }
		public RoundEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_roundE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterRoundE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitRoundE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitRoundE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoundEContext roundE() throws RecognitionException {
		RoundEContext _localctx = new RoundEContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_roundE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			match(OR);
			setState(61);
			e();
			setState(62);
			match(CR);
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
	public static class MGenContext extends ParserRuleContext {
		public TerminalNode OS() { return getToken(FearlessParser.OS, 0); }
		public TerminalNode CS() { return getToken(FearlessParser.CS, 0); }
		public List<TContext> t() {
			return getRuleContexts(TContext.class);
		}
		public TContext t(int i) {
			return getRuleContext(TContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public MGenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mGen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterMGen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitMGen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitMGen(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MGenContext mGen() throws RecognitionException {
		MGenContext _localctx = new MGenContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_mGen);
		int _la;
		try {
			setState(76);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
			case Mut:
			case Lent:
			case Read:
			case Iso:
			case RecMdf:
			case Mdf:
			case Imm:
			case As:
			case OC:
			case CC:
			case CS:
			case OR:
			case CR:
			case Comma:
			case Colon:
			case Arrow:
			case Underscore:
			case X:
			case MName:
			case SysInM:
			case FullCN:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case OS:
				enterOuterAlt(_localctx, 2);
				{
				setState(65);
				match(OS);
				setState(73);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 67109118L) != 0) {
					{
					setState(66);
					t();
					setState(69); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(67);
						match(Comma);
						setState(68);
						t();
						}
						}
						setState(71); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(75);
				match(CS);
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
	public static class LambdaContext extends ParserRuleContext {
		public MdfContext mdf() {
			return getRuleContext(MdfContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public LambdaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambda; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaContext lambda() throws RecognitionException {
		LambdaContext _localctx = new LambdaContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_lambda);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			mdf();
			setState(79);
			block();
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
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode OC() { return getToken(FearlessParser.OC, 0); }
		public BblockContext bblock() {
			return getRuleContext(BblockContext.class,0);
		}
		public TerminalNode CC() { return getToken(FearlessParser.CC, 0); }
		public TContext t() {
			return getRuleContext(TContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_block);
		int _la;
		try {
			setState(89);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(82);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 67109118L) != 0) {
					{
					setState(81);
					t();
					}
				}

				setState(84);
				match(OC);
				setState(85);
				bblock();
				setState(86);
				match(CC);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				t();
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

	@SuppressWarnings("CheckReturnValue")
	public static class BblockContext extends ParserRuleContext {
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public SingleMContext singleM() {
			return getRuleContext(SingleMContext.class,0);
		}
		public List<MethContext> meth() {
			return getRuleContexts(MethContext.class);
		}
		public MethContext meth(int i) {
			return getRuleContext(MethContext.class,i);
		}
		public BblockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bblock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterBblock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitBblock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitBblock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BblockContext bblock() throws RecognitionException {
		BblockContext _localctx = new BblockContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_bblock);
		int _la;
		try {
			setState(113);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				x();
				setState(93);
				match(Comma);
				setState(94);
				singleM();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(96);
				singleM();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(97);
				x();
				setState(98);
				match(Comma);
				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(99);
					meth();
					setState(100);
					match(Comma);
					}
					}
					setState(104); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 37748990L) != 0 );
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(109); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(106);
					meth();
					setState(107);
					match(Comma);
					}
					}
					setState(111); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 37748990L) != 0 );
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

	@SuppressWarnings("CheckReturnValue")
	public static class TContext extends ParserRuleContext {
		public MdfContext mdf() {
			return getRuleContext(MdfContext.class,0);
		}
		public FullCNContext fullCN() {
			return getRuleContext(FullCNContext.class,0);
		}
		public MGenContext mGen() {
			return getRuleContext(MGenContext.class,0);
		}
		public TContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_t; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TContext t() throws RecognitionException {
		TContext _localctx = new TContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_t);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			mdf();
			setState(116);
			fullCN();
			setState(117);
			mGen();
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
	public static class SingleMContext extends ParserRuleContext {
		public TerminalNode Arrow() { return getToken(FearlessParser.Arrow, 0); }
		public EContext e() {
			return getRuleContext(EContext.class,0);
		}
		public List<XContext> x() {
			return getRuleContexts(XContext.class);
		}
		public XContext x(int i) {
			return getRuleContext(XContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public SingleMContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleM; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterSingleM(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitSingleM(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitSingleM(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleMContext singleM() throws RecognitionException {
		SingleMContext _localctx = new SingleMContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_singleM);
		int _la;
		try {
			setState(131);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(126);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(119);
					x();
					setState(122); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(120);
						match(Comma);
						setState(121);
						x();
						}
						}
						setState(124); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(128);
				match(Arrow);
				setState(129);
				e();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(130);
				e();
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

	@SuppressWarnings("CheckReturnValue")
	public static class MethContext extends ParserRuleContext {
		public SigContext sig() {
			return getRuleContext(SigContext.class,0);
		}
		public TerminalNode Arrow() { return getToken(FearlessParser.Arrow, 0); }
		public EContext e() {
			return getRuleContext(EContext.class,0);
		}
		public MContext m() {
			return getRuleContext(MContext.class,0);
		}
		public TerminalNode OR() { return getToken(FearlessParser.OR, 0); }
		public TerminalNode CR() { return getToken(FearlessParser.CR, 0); }
		public List<XContext> x() {
			return getRuleContexts(XContext.class);
		}
		public XContext x(int i) {
			return getRuleContext(XContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public MethContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_meth; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterMeth(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitMeth(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitMeth(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethContext meth() throws RecognitionException {
		MethContext _localctx = new MethContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_meth);
		int _la;
		try {
			setState(153);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(133);
				sig();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(134);
				sig();
				setState(135);
				match(Arrow);
				setState(136);
				e();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(138);
				m();
				setState(139);
				match(OR);
				setState(147);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(140);
					x();
					setState(143); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(141);
						match(Comma);
						setState(142);
						x();
						}
						}
						setState(145); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(149);
				match(CR);
				setState(150);
				match(Arrow);
				setState(151);
				e();
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

	@SuppressWarnings("CheckReturnValue")
	public static class SigContext extends ParserRuleContext {
		public MdfContext mdf() {
			return getRuleContext(MdfContext.class,0);
		}
		public MContext m() {
			return getRuleContext(MContext.class,0);
		}
		public MGenContext mGen() {
			return getRuleContext(MGenContext.class,0);
		}
		public TerminalNode OR() { return getToken(FearlessParser.OR, 0); }
		public GammaContext gamma() {
			return getRuleContext(GammaContext.class,0);
		}
		public TerminalNode CR() { return getToken(FearlessParser.CR, 0); }
		public TerminalNode Colon() { return getToken(FearlessParser.Colon, 0); }
		public TContext t() {
			return getRuleContext(TContext.class,0);
		}
		public SigContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sig; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterSig(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitSig(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitSig(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SigContext sig() throws RecognitionException {
		SigContext _localctx = new SigContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_sig);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			mdf();
			setState(156);
			m();
			setState(157);
			mGen();
			setState(158);
			match(OR);
			setState(159);
			gamma();
			setState(160);
			match(CR);
			setState(161);
			match(Colon);
			setState(162);
			t();
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
	public static class GammaContext extends ParserRuleContext {
		public List<XContext> x() {
			return getRuleContexts(XContext.class);
		}
		public XContext x(int i) {
			return getRuleContext(XContext.class,i);
		}
		public List<TerminalNode> Colon() { return getTokens(FearlessParser.Colon); }
		public TerminalNode Colon(int i) {
			return getToken(FearlessParser.Colon, i);
		}
		public List<TContext> t() {
			return getRuleContexts(TContext.class);
		}
		public TContext t(int i) {
			return getRuleContext(TContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public GammaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gamma; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterGamma(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitGamma(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitGamma(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GammaContext gamma() throws RecognitionException {
		GammaContext _localctx = new GammaContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_gamma);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Underscore || _la==X) {
				{
				setState(164);
				x();
				setState(165);
				match(Colon);
				setState(166);
				t();
				setState(172); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(167);
					match(Comma);
					setState(168);
					x();
					setState(169);
					match(Colon);
					setState(170);
					t();
					}
					}
					setState(174); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==Comma );
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

	@SuppressWarnings("CheckReturnValue")
	public static class TopDecContext extends ParserRuleContext {
		public FullCNContext fullCN() {
			return getRuleContext(FullCNContext.class,0);
		}
		public MGenContext mGen() {
			return getRuleContext(MGenContext.class,0);
		}
		public TerminalNode Colon() { return getToken(FearlessParser.Colon, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TopDecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topDec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterTopDec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitTopDec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitTopDec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopDecContext topDec() throws RecognitionException {
		TopDecContext _localctx = new TopDecContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_topDec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			fullCN();
			setState(179);
			mGen();
			setState(180);
			match(Colon);
			setState(181);
			block();
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
	public static class AliasContext extends ParserRuleContext {
		public TerminalNode Alias() { return getToken(FearlessParser.Alias, 0); }
		public List<FullCNContext> fullCN() {
			return getRuleContexts(FullCNContext.class);
		}
		public FullCNContext fullCN(int i) {
			return getRuleContext(FullCNContext.class,i);
		}
		public List<MGenContext> mGen() {
			return getRuleContexts(MGenContext.class);
		}
		public MGenContext mGen(int i) {
			return getRuleContext(MGenContext.class,i);
		}
		public TerminalNode As() { return getToken(FearlessParser.As, 0); }
		public TerminalNode Comma() { return getToken(FearlessParser.Comma, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(Alias);
			setState(184);
			fullCN();
			setState(185);
			mGen();
			setState(186);
			match(As);
			setState(187);
			fullCN();
			setState(188);
			mGen();
			setState(189);
			match(Comma);
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
	public static class AtomEContext extends ParserRuleContext {
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public RoundEContext roundE() {
			return getRuleContext(RoundEContext.class,0);
		}
		public LambdaContext lambda() {
			return getRuleContext(LambdaContext.class,0);
		}
		public AtomEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterAtomE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitAtomE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitAtomE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomEContext atomE() throws RecognitionException {
		AtomEContext _localctx = new AtomEContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_atomE);
		try {
			setState(194);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Underscore:
			case X:
				enterOuterAlt(_localctx, 1);
				{
				setState(191);
				x();
				}
				break;
			case OR:
				enterOuterAlt(_localctx, 2);
				{
				setState(192);
				roundE();
				}
				break;
			case Mut:
			case Lent:
			case Read:
			case Iso:
			case RecMdf:
			case Mdf:
			case Imm:
			case OC:
			case FullCN:
				enterOuterAlt(_localctx, 3);
				{
				setState(193);
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
	public static class PostEContext extends ParserRuleContext {
		public AtomEContext atomE() {
			return getRuleContext(AtomEContext.class,0);
		}
		public List<POpContext> pOp() {
			return getRuleContexts(POpContext.class);
		}
		public POpContext pOp(int i) {
			return getRuleContext(POpContext.class,i);
		}
		public PostEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterPostE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitPostE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitPostE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostEContext postE() throws RecognitionException {
		PostEContext _localctx = new PostEContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_postE);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			atomE();
			setState(200);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(197);
					pOp();
					}
					} 
				}
				setState(202);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class POpContext extends ParserRuleContext {
		public MContext m() {
			return getRuleContext(MContext.class,0);
		}
		public MGenContext mGen() {
			return getRuleContext(MGenContext.class,0);
		}
		public TerminalNode OR() { return getToken(FearlessParser.OR, 0); }
		public TerminalNode CR() { return getToken(FearlessParser.CR, 0); }
		public List<EContext> e() {
			return getRuleContexts(EContext.class);
		}
		public EContext e(int i) {
			return getRuleContext(EContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public TerminalNode Eq() { return getToken(FearlessParser.Eq, 0); }
		public POpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterPOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitPOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitPOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final POpContext pOp() throws RecognitionException {
		POpContext _localctx = new POpContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_pOp);
		int _la;
		try {
			setState(228);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(203);
				m();
				setState(204);
				mGen();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(206);
				m();
				setState(207);
				mGen();
				setState(208);
				match(OR);
				setState(216);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 70289662L) != 0) {
					{
					setState(209);
					e();
					setState(212); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(210);
						match(Comma);
						setState(211);
						e();
						}
						}
						setState(214); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(218);
				match(CR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(220);
				m();
				setState(221);
				mGen();
				setState(222);
				match(OR);
				setState(223);
				x();
				setState(224);
				match(Eq);
				setState(225);
				e();
				setState(226);
				match(CR);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EContext extends ParserRuleContext {
		public List<PostEContext> postE() {
			return getRuleContexts(PostEContext.class);
		}
		public PostEContext postE(int i) {
			return getRuleContext(PostEContext.class,i);
		}
		public List<MContext> m() {
			return getRuleContexts(MContext.class);
		}
		public MContext m(int i) {
			return getRuleContext(MContext.class,i);
		}
		public List<MGenContext> mGen() {
			return getRuleContexts(MGenContext.class);
		}
		public MGenContext mGen(int i) {
			return getRuleContext(MGenContext.class,i);
		}
		public List<XContext> x() {
			return getRuleContexts(XContext.class);
		}
		public XContext x(int i) {
			return getRuleContext(XContext.class,i);
		}
		public List<TerminalNode> Eq() { return getTokens(FearlessParser.Eq); }
		public TerminalNode Eq(int i) {
			return getToken(FearlessParser.Eq, i);
		}
		public EContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_e; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EContext e() throws RecognitionException {
		EContext _localctx = new EContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_e);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			postE();
			setState(242);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MName || _la==SysInM) {
				{
				{
				setState(231);
				m();
				setState(232);
				mGen();
				setState(236);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
				case 1:
					{
					setState(233);
					x();
					setState(234);
					match(Eq);
					}
					break;
				}
				setState(238);
				postE();
				}
				}
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class NudeEContext extends ParserRuleContext {
		public EContext e() {
			return getRuleContext(EContext.class,0);
		}
		public TerminalNode EOF() { return getToken(FearlessParser.EOF, 0); }
		public NudeEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterNudeE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitNudeE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitNudeE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeEContext nudeE() throws RecognitionException {
		NudeEContext _localctx = new NudeEContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_nudeE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			e();
			setState(246);
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

	@SuppressWarnings("CheckReturnValue")
	public static class NudeProgramContext extends ParserRuleContext {
		public TerminalNode Pack() { return getToken(FearlessParser.Pack, 0); }
		public TerminalNode EOF() { return getToken(FearlessParser.EOF, 0); }
		public List<AliasContext> alias() {
			return getRuleContexts(AliasContext.class);
		}
		public AliasContext alias(int i) {
			return getRuleContext(AliasContext.class,i);
		}
		public List<TopDecContext> topDec() {
			return getRuleContexts(TopDecContext.class);
		}
		public TopDecContext topDec(int i) {
			return getRuleContext(TopDecContext.class,i);
		}
		public NudeProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeProgram; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterNudeProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitNudeProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitNudeProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeProgramContext nudeProgram() throws RecognitionException {
		NudeProgramContext _localctx = new NudeProgramContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_nudeProgram);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			match(Pack);
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Alias) {
				{
				{
				setState(249);
				alias();
				}
				}
				setState(254);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FullCN) {
				{
				{
				setState(255);
				topDec();
				}
				}
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(261);
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
		"\u0004\u0001\u001c\u0108\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003;\b\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0004\u0005F\b\u0005\u000b\u0005\f\u0005G\u0003\u0005"+
		"J\b\u0005\u0001\u0005\u0003\u0005M\b\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0003\u0007S\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007Z\b\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0004"+
		"\bg\b\b\u000b\b\f\bh\u0001\b\u0001\b\u0001\b\u0004\bn\b\b\u000b\b\f\b"+
		"o\u0003\br\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n"+
		"\u0004\n{\b\n\u000b\n\f\n|\u0003\n\u007f\b\n\u0001\n\u0001\n\u0001\n\u0003"+
		"\n\u0084\b\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0004\u000b"+
		"\u0090\b\u000b\u000b\u000b\f\u000b\u0091\u0003\u000b\u0094\b\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u009a\b\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0004\r\u00ad"+
		"\b\r\u000b\r\f\r\u00ae\u0003\r\u00b1\b\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0003\u0010\u00c3\b\u0010\u0001\u0011\u0001\u0011\u0005"+
		"\u0011\u00c7\b\u0011\n\u0011\f\u0011\u00ca\t\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0004\u0012\u00d5\b\u0012\u000b\u0012\f\u0012\u00d6\u0003"+
		"\u0012\u00d9\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003"+
		"\u0012\u00e5\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0003\u0013\u00ed\b\u0013\u0001\u0013\u0001\u0013\u0005"+
		"\u0013\u00f1\b\u0013\n\u0013\f\u0013\u00f4\t\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0015\u0001\u0015\u0005\u0015\u00fb\b\u0015\n\u0015"+
		"\f\u0015\u00fe\t\u0015\u0001\u0015\u0005\u0015\u0101\b\u0015\n\u0015\f"+
		"\u0015\u0104\t\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0000\u0000\u0016"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*\u0000\u0002\u0001\u0000\u0014\u0015\u0002\u0000\u0016"+
		"\u0016\u0019\u0019\u0117\u0000,\u0001\u0000\u0000\u0000\u0002.\u0001\u0000"+
		"\u0000\u0000\u00040\u0001\u0000\u0000\u0000\u0006:\u0001\u0000\u0000\u0000"+
		"\b<\u0001\u0000\u0000\u0000\nL\u0001\u0000\u0000\u0000\fN\u0001\u0000"+
		"\u0000\u0000\u000eY\u0001\u0000\u0000\u0000\u0010q\u0001\u0000\u0000\u0000"+
		"\u0012s\u0001\u0000\u0000\u0000\u0014\u0083\u0001\u0000\u0000\u0000\u0016"+
		"\u0099\u0001\u0000\u0000\u0000\u0018\u009b\u0001\u0000\u0000\u0000\u001a"+
		"\u00b0\u0001\u0000\u0000\u0000\u001c\u00b2\u0001\u0000\u0000\u0000\u001e"+
		"\u00b7\u0001\u0000\u0000\u0000 \u00c2\u0001\u0000\u0000\u0000\"\u00c4"+
		"\u0001\u0000\u0000\u0000$\u00e4\u0001\u0000\u0000\u0000&\u00e6\u0001\u0000"+
		"\u0000\u0000(\u00f5\u0001\u0000\u0000\u0000*\u00f8\u0001\u0000\u0000\u0000"+
		",-\u0005\u001a\u0000\u0000-\u0001\u0001\u0000\u0000\u0000./\u0007\u0000"+
		"\u0000\u0000/\u0003\u0001\u0000\u0000\u000001\u0007\u0001\u0000\u0000"+
		"1\u0005\u0001\u0000\u0000\u00002;\u0001\u0000\u0000\u00003;\u0005\u0001"+
		"\u0000\u00004;\u0005\u0002\u0000\u00005;\u0005\u0003\u0000\u00006;\u0005"+
		"\u0004\u0000\u00007;\u0005\u0005\u0000\u00008;\u0005\u0006\u0000\u0000"+
		"9;\u0005\u0007\u0000\u0000:2\u0001\u0000\u0000\u0000:3\u0001\u0000\u0000"+
		"\u0000:4\u0001\u0000\u0000\u0000:5\u0001\u0000\u0000\u0000:6\u0001\u0000"+
		"\u0000\u0000:7\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000:9\u0001"+
		"\u0000\u0000\u0000;\u0007\u0001\u0000\u0000\u0000<=\u0005\u000f\u0000"+
		"\u0000=>\u0003&\u0013\u0000>?\u0005\u0010\u0000\u0000?\t\u0001\u0000\u0000"+
		"\u0000@M\u0001\u0000\u0000\u0000AI\u0005\r\u0000\u0000BE\u0003\u0012\t"+
		"\u0000CD\u0005\u0011\u0000\u0000DF\u0003\u0012\t\u0000EC\u0001\u0000\u0000"+
		"\u0000FG\u0001\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000GH\u0001\u0000"+
		"\u0000\u0000HJ\u0001\u0000\u0000\u0000IB\u0001\u0000\u0000\u0000IJ\u0001"+
		"\u0000\u0000\u0000JK\u0001\u0000\u0000\u0000KM\u0005\u000e\u0000\u0000"+
		"L@\u0001\u0000\u0000\u0000LA\u0001\u0000\u0000\u0000M\u000b\u0001\u0000"+
		"\u0000\u0000NO\u0003\u0006\u0003\u0000OP\u0003\u000e\u0007\u0000P\r\u0001"+
		"\u0000\u0000\u0000QS\u0003\u0012\t\u0000RQ\u0001\u0000\u0000\u0000RS\u0001"+
		"\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000TU\u0005\u000b\u0000\u0000"+
		"UV\u0003\u0010\b\u0000VW\u0005\f\u0000\u0000WZ\u0001\u0000\u0000\u0000"+
		"XZ\u0003\u0012\t\u0000YR\u0001\u0000\u0000\u0000YX\u0001\u0000\u0000\u0000"+
		"Z\u000f\u0001\u0000\u0000\u0000[r\u0001\u0000\u0000\u0000\\]\u0003\u0002"+
		"\u0001\u0000]^\u0005\u0011\u0000\u0000^_\u0003\u0014\n\u0000_r\u0001\u0000"+
		"\u0000\u0000`r\u0003\u0014\n\u0000ab\u0003\u0002\u0001\u0000bf\u0005\u0011"+
		"\u0000\u0000cd\u0003\u0016\u000b\u0000de\u0005\u0011\u0000\u0000eg\u0001"+
		"\u0000\u0000\u0000fc\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000"+
		"hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000ir\u0001\u0000\u0000"+
		"\u0000jk\u0003\u0016\u000b\u0000kl\u0005\u0011\u0000\u0000ln\u0001\u0000"+
		"\u0000\u0000mj\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000om\u0001"+
		"\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pr\u0001\u0000\u0000\u0000"+
		"q[\u0001\u0000\u0000\u0000q\\\u0001\u0000\u0000\u0000q`\u0001\u0000\u0000"+
		"\u0000qa\u0001\u0000\u0000\u0000qm\u0001\u0000\u0000\u0000r\u0011\u0001"+
		"\u0000\u0000\u0000st\u0003\u0006\u0003\u0000tu\u0003\u0000\u0000\u0000"+
		"uv\u0003\n\u0005\u0000v\u0013\u0001\u0000\u0000\u0000wz\u0003\u0002\u0001"+
		"\u0000xy\u0005\u0011\u0000\u0000y{\u0003\u0002\u0001\u0000zx\u0001\u0000"+
		"\u0000\u0000{|\u0001\u0000\u0000\u0000|z\u0001\u0000\u0000\u0000|}\u0001"+
		"\u0000\u0000\u0000}\u007f\u0001\u0000\u0000\u0000~w\u0001\u0000\u0000"+
		"\u0000~\u007f\u0001\u0000\u0000\u0000\u007f\u0080\u0001\u0000\u0000\u0000"+
		"\u0080\u0081\u0005\u0013\u0000\u0000\u0081\u0084\u0003&\u0013\u0000\u0082"+
		"\u0084\u0003&\u0013\u0000\u0083~\u0001\u0000\u0000\u0000\u0083\u0082\u0001"+
		"\u0000\u0000\u0000\u0084\u0015\u0001\u0000\u0000\u0000\u0085\u009a\u0003"+
		"\u0018\f\u0000\u0086\u0087\u0003\u0018\f\u0000\u0087\u0088\u0005\u0013"+
		"\u0000\u0000\u0088\u0089\u0003&\u0013\u0000\u0089\u009a\u0001\u0000\u0000"+
		"\u0000\u008a\u008b\u0003\u0004\u0002\u0000\u008b\u0093\u0005\u000f\u0000"+
		"\u0000\u008c\u008f\u0003\u0002\u0001\u0000\u008d\u008e\u0005\u0011\u0000"+
		"\u0000\u008e\u0090\u0003\u0002\u0001\u0000\u008f\u008d\u0001\u0000\u0000"+
		"\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u008f\u0001\u0000\u0000"+
		"\u0000\u0091\u0092\u0001\u0000\u0000\u0000\u0092\u0094\u0001\u0000\u0000"+
		"\u0000\u0093\u008c\u0001\u0000\u0000\u0000\u0093\u0094\u0001\u0000\u0000"+
		"\u0000\u0094\u0095\u0001\u0000\u0000\u0000\u0095\u0096\u0005\u0010\u0000"+
		"\u0000\u0096\u0097\u0005\u0013\u0000\u0000\u0097\u0098\u0003&\u0013\u0000"+
		"\u0098\u009a\u0001\u0000\u0000\u0000\u0099\u0085\u0001\u0000\u0000\u0000"+
		"\u0099\u0086\u0001\u0000\u0000\u0000\u0099\u008a\u0001\u0000\u0000\u0000"+
		"\u009a\u0017\u0001\u0000\u0000\u0000\u009b\u009c\u0003\u0006\u0003\u0000"+
		"\u009c\u009d\u0003\u0004\u0002\u0000\u009d\u009e\u0003\n\u0005\u0000\u009e"+
		"\u009f\u0005\u000f\u0000\u0000\u009f\u00a0\u0003\u001a\r\u0000\u00a0\u00a1"+
		"\u0005\u0010\u0000\u0000\u00a1\u00a2\u0005\u0012\u0000\u0000\u00a2\u00a3"+
		"\u0003\u0012\t\u0000\u00a3\u0019\u0001\u0000\u0000\u0000\u00a4\u00a5\u0003"+
		"\u0002\u0001\u0000\u00a5\u00a6\u0005\u0012\u0000\u0000\u00a6\u00ac\u0003"+
		"\u0012\t\u0000\u00a7\u00a8\u0005\u0011\u0000\u0000\u00a8\u00a9\u0003\u0002"+
		"\u0001\u0000\u00a9\u00aa\u0005\u0012\u0000\u0000\u00aa\u00ab\u0003\u0012"+
		"\t\u0000\u00ab\u00ad\u0001\u0000\u0000\u0000\u00ac\u00a7\u0001\u0000\u0000"+
		"\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00ac\u0001\u0000\u0000"+
		"\u0000\u00ae\u00af\u0001\u0000\u0000\u0000\u00af\u00b1\u0001\u0000\u0000"+
		"\u0000\u00b0\u00a4\u0001\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000"+
		"\u0000\u00b1\u001b\u0001\u0000\u0000\u0000\u00b2\u00b3\u0003\u0000\u0000"+
		"\u0000\u00b3\u00b4\u0003\n\u0005\u0000\u00b4\u00b5\u0005\u0012\u0000\u0000"+
		"\u00b5\u00b6\u0003\u000e\u0007\u0000\u00b6\u001d\u0001\u0000\u0000\u0000"+
		"\u00b7\u00b8\u0005\t\u0000\u0000\u00b8\u00b9\u0003\u0000\u0000\u0000\u00b9"+
		"\u00ba\u0003\n\u0005\u0000\u00ba\u00bb\u0005\n\u0000\u0000\u00bb\u00bc"+
		"\u0003\u0000\u0000\u0000\u00bc\u00bd\u0003\n\u0005\u0000\u00bd\u00be\u0005"+
		"\u0011\u0000\u0000\u00be\u001f\u0001\u0000\u0000\u0000\u00bf\u00c3\u0003"+
		"\u0002\u0001\u0000\u00c0\u00c3\u0003\b\u0004\u0000\u00c1\u00c3\u0003\f"+
		"\u0006\u0000\u00c2\u00bf\u0001\u0000\u0000\u0000\u00c2\u00c0\u0001\u0000"+
		"\u0000\u0000\u00c2\u00c1\u0001\u0000\u0000\u0000\u00c3!\u0001\u0000\u0000"+
		"\u0000\u00c4\u00c8\u0003 \u0010\u0000\u00c5\u00c7\u0003$\u0012\u0000\u00c6"+
		"\u00c5\u0001\u0000\u0000\u0000\u00c7\u00ca\u0001\u0000\u0000\u0000\u00c8"+
		"\u00c6\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000\u00c9"+
		"#\u0001\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00cb\u00cc"+
		"\u0003\u0004\u0002\u0000\u00cc\u00cd\u0003\n\u0005\u0000\u00cd\u00e5\u0001"+
		"\u0000\u0000\u0000\u00ce\u00cf\u0003\u0004\u0002\u0000\u00cf\u00d0\u0003"+
		"\n\u0005\u0000\u00d0\u00d8\u0005\u000f\u0000\u0000\u00d1\u00d4\u0003&"+
		"\u0013\u0000\u00d2\u00d3\u0005\u0011\u0000\u0000\u00d3\u00d5\u0003&\u0013"+
		"\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d6\u00d4\u0001\u0000\u0000\u0000\u00d6\u00d7\u0001\u0000\u0000"+
		"\u0000\u00d7\u00d9\u0001\u0000\u0000\u0000\u00d8\u00d1\u0001\u0000\u0000"+
		"\u0000\u00d8\u00d9\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000\u0000"+
		"\u0000\u00da\u00db\u0005\u0010\u0000\u0000\u00db\u00e5\u0001\u0000\u0000"+
		"\u0000\u00dc\u00dd\u0003\u0004\u0002\u0000\u00dd\u00de\u0003\n\u0005\u0000"+
		"\u00de\u00df\u0005\u000f\u0000\u0000\u00df\u00e0\u0003\u0002\u0001\u0000"+
		"\u00e0\u00e1\u0005\b\u0000\u0000\u00e1\u00e2\u0003&\u0013\u0000\u00e2"+
		"\u00e3\u0005\u0010\u0000\u0000\u00e3\u00e5\u0001\u0000\u0000\u0000\u00e4"+
		"\u00cb\u0001\u0000\u0000\u0000\u00e4\u00ce\u0001\u0000\u0000\u0000\u00e4"+
		"\u00dc\u0001\u0000\u0000\u0000\u00e5%\u0001\u0000\u0000\u0000\u00e6\u00f2"+
		"\u0003\"\u0011\u0000\u00e7\u00e8\u0003\u0004\u0002\u0000\u00e8\u00ec\u0003"+
		"\n\u0005\u0000\u00e9\u00ea\u0003\u0002\u0001\u0000\u00ea\u00eb\u0005\b"+
		"\u0000\u0000\u00eb\u00ed\u0001\u0000\u0000\u0000\u00ec\u00e9\u0001\u0000"+
		"\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000"+
		"\u0000\u0000\u00ee\u00ef\u0003\"\u0011\u0000\u00ef\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f0\u00e7\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000\u0000"+
		"\u0000\u00f2\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000"+
		"\u0000\u00f3\'\u0001\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f6\u0003&\u0013\u0000\u00f6\u00f7\u0005\u0000\u0000\u0001\u00f7"+
		")\u0001\u0000\u0000\u0000\u00f8\u00fc\u0005\u001c\u0000\u0000\u00f9\u00fb"+
		"\u0003\u001e\u000f\u0000\u00fa\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fe"+
		"\u0001\u0000\u0000\u0000\u00fc\u00fa\u0001\u0000\u0000\u0000\u00fc\u00fd"+
		"\u0001\u0000\u0000\u0000\u00fd\u0102\u0001\u0000\u0000\u0000\u00fe\u00fc"+
		"\u0001\u0000\u0000\u0000\u00ff\u0101\u0003\u001c\u000e\u0000\u0100\u00ff"+
		"\u0001\u0000\u0000\u0000\u0101\u0104\u0001\u0000\u0000\u0000\u0102\u0100"+
		"\u0001\u0000\u0000\u0000\u0102\u0103\u0001\u0000\u0000\u0000\u0103\u0105"+
		"\u0001\u0000\u0000\u0000\u0104\u0102\u0001\u0000\u0000\u0000\u0105\u0106"+
		"\u0005\u0000\u0000\u0001\u0106+\u0001\u0000\u0000\u0000\u001a:GILRYho"+
		"q|~\u0083\u0091\u0093\u0099\u00ae\u00b0\u00c2\u00c8\u00d6\u00d8\u00e4"+
		"\u00ec\u00f2\u00fc\u0102";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}