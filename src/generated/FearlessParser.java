// Generated from /home/nick/Programming/uni/fearless/antlrGrammars/Fearless.g4 by ANTLR 4.12.0
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
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Mut=1, Lent=2, Read=3, ReadOnly=4, Iso=5, RecMdf=6, Mdf=7, Imm=8, Eq=9, 
		Alias=10, As=11, OC=12, CC=13, OS=14, CS=15, OR=16, CR=17, Comma=18, Colon=19, 
		Arrow=20, Underscore=21, X=22, SelfX=23, MName=24, BlockComment=25, LineComment=26, 
		SysInM=27, FullCN=28, Whitespace=29, Pack=30;
	public static final int
		RULE_fullCN = 0, RULE_x = 1, RULE_m = 2, RULE_mdf = 3, RULE_roundE = 4, 
		RULE_genDecl = 5, RULE_mGen = 6, RULE_lambda = 7, RULE_block = 8, RULE_bblock = 9, 
		RULE_t = 10, RULE_singleM = 11, RULE_meth = 12, RULE_sig = 13, RULE_gamma = 14, 
		RULE_topDec = 15, RULE_alias = 16, RULE_atomE = 17, RULE_postE = 18, RULE_pOp = 19, 
		RULE_e = 20, RULE_callOp = 21, RULE_nudeE = 22, RULE_nudeX = 23, RULE_nudeM = 24, 
		RULE_nudeFullCN = 25, RULE_nudeT = 26, RULE_nudeProgram = 27;
	private static String[] makeRuleNames() {
		return new String[] {
			"fullCN", "x", "m", "mdf", "roundE", "genDecl", "mGen", "lambda", "block", 
			"bblock", "t", "singleM", "meth", "sig", "gamma", "topDec", "alias", 
			"atomE", "postE", "pOp", "e", "callOp", "nudeE", "nudeX", "nudeM", "nudeFullCN", 
			"nudeT", "nudeProgram"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'mut'", "'lent'", "'read'", "'readOnly'", "'iso'", "'recMdf'", 
			"'mdf'", "'imm'", "'='", "'alias'", "'as'", "'{'", "'}'", "'['", "']'", 
			"'('", "')'", "','", "':'", "'->'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Mut", "Lent", "Read", "ReadOnly", "Iso", "RecMdf", "Mdf", "Imm", 
			"Eq", "Alias", "As", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", 
			"Arrow", "Underscore", "X", "SelfX", "MName", "BlockComment", "LineComment", 
			"SysInM", "FullCN", "Whitespace", "Pack"
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
	public String getGrammarFileName() { return "Fearless.g4"; }

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
			setState(56);
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
			setState(58);
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
			setState(60);
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
		public TerminalNode ReadOnly() { return getToken(FearlessParser.ReadOnly, 0); }
		public TerminalNode Lent() { return getToken(FearlessParser.Lent, 0); }
		public TerminalNode Read() { return getToken(FearlessParser.Read, 0); }
		public TerminalNode Iso() { return getToken(FearlessParser.Iso, 0); }
		public TerminalNode RecMdf() { return getToken(FearlessParser.RecMdf, 0); }
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
			setState(70);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(62);
				match(Mut);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(63);
				match(ReadOnly);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				match(Lent);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(65);
				match(Read);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(66);
				match(Iso);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(67);
				match(RecMdf);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(68);
				match(Imm);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
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
			setState(72);
			match(OR);
			setState(73);
			e();
			setState(74);
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
	public static class GenDeclContext extends ParserRuleContext {
		public TContext t() {
			return getRuleContext(TContext.class,0);
		}
		public TerminalNode Colon() { return getToken(FearlessParser.Colon, 0); }
		public List<MdfContext> mdf() {
			return getRuleContexts(MdfContext.class);
		}
		public MdfContext mdf(int i) {
			return getRuleContext(MdfContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
		}
		public GenDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterGenDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitGenDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitGenDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenDeclContext genDecl() throws RecognitionException {
		GenDeclContext _localctx = new GenDeclContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_genDecl);
		try {
			int _alt;
			setState(87);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(76);
				t();
				setState(77);
				match(Colon);
				{
				setState(78);
				mdf();
				setState(83);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(79);
						match(Comma);
						setState(80);
						mdf();
						}
						} 
					}
					setState(85);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				}
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
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
	public static class MGenContext extends ParserRuleContext {
		public TerminalNode OS() { return getToken(FearlessParser.OS, 0); }
		public TerminalNode CS() { return getToken(FearlessParser.CS, 0); }
		public List<GenDeclContext> genDecl() {
			return getRuleContexts(GenDeclContext.class);
		}
		public GenDeclContext genDecl(int i) {
			return getRuleContext(GenDeclContext.class,i);
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
		enterRule(_localctx, 12, RULE_mGen);
		int _la;
		try {
			setState(102);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
			case Mut:
			case Lent:
			case Read:
			case ReadOnly:
			case Iso:
			case RecMdf:
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
				setState(90);
				match(OS);
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 268435838L) != 0)) {
					{
					setState(91);
					genDecl();
					setState(96);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(92);
						match(Comma);
						setState(93);
						genDecl();
						}
						}
						setState(98);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(101);
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
		public TopDecContext topDec() {
			return getRuleContext(TopDecContext.class,0);
		}
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
		enterRule(_localctx, 14, RULE_lambda);
		try {
			setState(108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(104);
				topDec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(105);
				mdf();
				setState(106);
				block();
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
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode OC() { return getToken(FearlessParser.OC, 0); }
		public BblockContext bblock() {
			return getRuleContext(BblockContext.class,0);
		}
		public TerminalNode CC() { return getToken(FearlessParser.CC, 0); }
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
		enterRule(_localctx, 16, RULE_block);
		int _la;
		try {
			setState(125);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(118);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 268435838L) != 0)) {
					{
					setState(110);
					t();
					setState(115);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(111);
						match(Comma);
						setState(112);
						t();
						}
						}
						setState(117);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(120);
				match(OC);
				setState(121);
				bblock();
				setState(122);
				match(CC);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
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
		public SingleMContext singleM() {
			return getRuleContext(SingleMContext.class,0);
		}
		public TerminalNode SelfX() { return getToken(FearlessParser.SelfX, 0); }
		public List<MethContext> meth() {
			return getRuleContexts(MethContext.class);
		}
		public MethContext meth(int i) {
			return getRuleContext(MethContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(FearlessParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(FearlessParser.Comma, i);
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
		enterRule(_localctx, 18, RULE_bblock);
		int _la;
		try {
			int _alt;
			setState(148);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(129);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SelfX) {
					{
					setState(128);
					match(SelfX);
					}
				}

				setState(131);
				singleM();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(133);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SelfX) {
					{
					setState(132);
					match(SelfX);
					}
				}

				setState(143);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 150995326L) != 0)) {
					{
					setState(135);
					meth();
					setState(140);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(136);
							match(Comma);
							setState(137);
							meth();
							}
							} 
						}
						setState(142);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
					}
					}
				}

				setState(146);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(145);
					match(Comma);
					}
				}

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
		enterRule(_localctx, 20, RULE_t);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			mdf();
			setState(151);
			fullCN();
			setState(152);
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
		enterRule(_localctx, 22, RULE_singleM);
		int _la;
		try {
			setState(167);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(154);
					x();
					setState(159);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(155);
						match(Comma);
						setState(156);
						x();
						}
						}
						setState(161);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(164);
				match(Arrow);
				setState(165);
				e();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(166);
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
		enterRule(_localctx, 24, RULE_meth);
		int _la;
		try {
			setState(204);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(169);
				sig();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(170);
				sig();
				setState(171);
				match(Arrow);
				setState(172);
				e();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(174);
				m();
				setState(175);
				match(OR);
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(176);
					x();
					setState(181);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(177);
						match(Comma);
						setState(178);
						x();
						}
						}
						setState(183);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(186);
				match(CR);
				setState(187);
				match(Arrow);
				setState(188);
				e();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(190);
				m();
				setState(199);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(191);
					x();
					setState(196);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(192);
						match(Comma);
						setState(193);
						x();
						}
						}
						setState(198);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(201);
				match(Arrow);
				setState(202);
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
		public TerminalNode Colon() { return getToken(FearlessParser.Colon, 0); }
		public TContext t() {
			return getRuleContext(TContext.class,0);
		}
		public TerminalNode OR() { return getToken(FearlessParser.OR, 0); }
		public GammaContext gamma() {
			return getRuleContext(GammaContext.class,0);
		}
		public TerminalNode CR() { return getToken(FearlessParser.CR, 0); }
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
		enterRule(_localctx, 26, RULE_sig);
		int _la;
		try {
			setState(225);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(206);
				mdf();
				setState(207);
				m();
				setState(208);
				mGen();
				setState(213);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(209);
					match(OR);
					setState(210);
					gamma();
					setState(211);
					match(CR);
					}
				}

				setState(215);
				match(Colon);
				setState(216);
				t();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(218);
				mdf();
				setState(219);
				m();
				setState(220);
				mGen();
				setState(221);
				gamma();
				setState(222);
				match(Colon);
				setState(223);
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
		enterRule(_localctx, 28, RULE_gamma);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Underscore || _la==X) {
				{
				setState(227);
				x();
				setState(228);
				match(Colon);
				setState(229);
				t();
				setState(237);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==Comma) {
					{
					{
					setState(230);
					match(Comma);
					setState(231);
					x();
					setState(232);
					match(Colon);
					setState(233);
					t();
					}
					}
					setState(239);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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
		enterRule(_localctx, 30, RULE_topDec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			fullCN();
			setState(243);
			mGen();
			setState(244);
			match(Colon);
			setState(245);
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
		enterRule(_localctx, 32, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(Alias);
			setState(248);
			fullCN();
			setState(249);
			mGen();
			setState(250);
			match(As);
			setState(251);
			fullCN();
			setState(252);
			mGen();
			setState(253);
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
		enterRule(_localctx, 34, RULE_atomE);
		try {
			setState(258);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Underscore:
			case X:
				enterOuterAlt(_localctx, 1);
				{
				setState(255);
				x();
				}
				break;
			case OR:
				enterOuterAlt(_localctx, 2);
				{
				setState(256);
				roundE();
				}
				break;
			case Mut:
			case Lent:
			case Read:
			case ReadOnly:
			case Iso:
			case RecMdf:
			case Imm:
			case OC:
			case FullCN:
				enterOuterAlt(_localctx, 3);
				{
				setState(257);
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
		enterRule(_localctx, 36, RULE_postE);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(260);
			atomE();
			setState(264);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(261);
					pOp();
					}
					} 
				}
				setState(266);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
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
		public List<CallOpContext> callOp() {
			return getRuleContexts(CallOpContext.class);
		}
		public CallOpContext callOp(int i) {
			return getRuleContext(CallOpContext.class,i);
		}
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
		enterRule(_localctx, 38, RULE_pOp);
		int _la;
		try {
			int _alt;
			setState(297);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(267);
				m();
				setState(268);
				mGen();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(270);
				m();
				setState(271);
				mGen();
				setState(272);
				match(OR);
				setState(280);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 274796926L) != 0)) {
					{
					setState(273);
					e();
					setState(276); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(274);
						match(Comma);
						setState(275);
						e();
						}
						}
						setState(278); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(282);
				match(CR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(284);
				m();
				setState(285);
				mGen();
				setState(286);
				match(OR);
				setState(287);
				x();
				setState(288);
				match(Eq);
				setState(289);
				e();
				setState(290);
				match(CR);
				setState(294);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(291);
						callOp();
						}
						} 
					}
					setState(296);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
				}
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
		public PostEContext postE() {
			return getRuleContext(PostEContext.class,0);
		}
		public List<CallOpContext> callOp() {
			return getRuleContexts(CallOpContext.class);
		}
		public CallOpContext callOp(int i) {
			return getRuleContext(CallOpContext.class,i);
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
		enterRule(_localctx, 40, RULE_e);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			postE();
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MName || _la==SysInM) {
				{
				{
				setState(300);
				callOp();
				}
				}
				setState(305);
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
	public static class CallOpContext extends ParserRuleContext {
		public MContext m() {
			return getRuleContext(MContext.class,0);
		}
		public MGenContext mGen() {
			return getRuleContext(MGenContext.class,0);
		}
		public PostEContext postE() {
			return getRuleContext(PostEContext.class,0);
		}
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public TerminalNode Eq() { return getToken(FearlessParser.Eq, 0); }
		public CallOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterCallOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitCallOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitCallOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallOpContext callOp() throws RecognitionException {
		CallOpContext _localctx = new CallOpContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_callOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			m();
			setState(307);
			mGen();
			setState(311);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(308);
				x();
				setState(309);
				match(Eq);
				}
				break;
			}
			setState(313);
			postE();
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
		enterRule(_localctx, 44, RULE_nudeE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(315);
			e();
			setState(316);
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
	public static class NudeXContext extends ParserRuleContext {
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public TerminalNode EOF() { return getToken(FearlessParser.EOF, 0); }
		public NudeXContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeX; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterNudeX(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitNudeX(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitNudeX(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeXContext nudeX() throws RecognitionException {
		NudeXContext _localctx = new NudeXContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_nudeX);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			x();
			setState(319);
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
	public static class NudeMContext extends ParserRuleContext {
		public MContext m() {
			return getRuleContext(MContext.class,0);
		}
		public TerminalNode EOF() { return getToken(FearlessParser.EOF, 0); }
		public NudeMContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeM; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterNudeM(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitNudeM(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitNudeM(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeMContext nudeM() throws RecognitionException {
		NudeMContext _localctx = new NudeMContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_nudeM);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(321);
			m();
			setState(322);
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
	public static class NudeFullCNContext extends ParserRuleContext {
		public FullCNContext fullCN() {
			return getRuleContext(FullCNContext.class,0);
		}
		public TerminalNode EOF() { return getToken(FearlessParser.EOF, 0); }
		public NudeFullCNContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeFullCN; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterNudeFullCN(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitNudeFullCN(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitNudeFullCN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeFullCNContext nudeFullCN() throws RecognitionException {
		NudeFullCNContext _localctx = new NudeFullCNContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_nudeFullCN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			fullCN();
			setState(325);
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
	public static class NudeTContext extends ParserRuleContext {
		public TContext t() {
			return getRuleContext(TContext.class,0);
		}
		public TerminalNode EOF() { return getToken(FearlessParser.EOF, 0); }
		public NudeTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nudeT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterNudeT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitNudeT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitNudeT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NudeTContext nudeT() throws RecognitionException {
		NudeTContext _localctx = new NudeTContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_nudeT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			t();
			setState(328);
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
		enterRule(_localctx, 54, RULE_nudeProgram);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(330);
			match(Pack);
			setState(334);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Alias) {
				{
				{
				setState(331);
				alias();
				}
				}
				setState(336);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(340);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FullCN) {
				{
				{
				setState(337);
				topDec();
				}
				}
				setState(342);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(343);
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
		"\u0004\u0001\u001e\u015a\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003G\b\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0005\u0005R\b\u0005\n\u0005\f\u0005U\t\u0005\u0001"+
		"\u0005\u0003\u0005X\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0005\u0006_\b\u0006\n\u0006\f\u0006b\t\u0006\u0003"+
		"\u0006d\b\u0006\u0001\u0006\u0003\u0006g\b\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007m\b\u0007\u0001\b\u0001\b\u0001\b"+
		"\u0005\br\b\b\n\b\f\bu\t\b\u0003\bw\b\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0003\b~\b\b\u0001\t\u0001\t\u0003\t\u0082\b\t\u0001\t\u0001"+
		"\t\u0003\t\u0086\b\t\u0001\t\u0001\t\u0001\t\u0005\t\u008b\b\t\n\t\f\t"+
		"\u008e\t\t\u0003\t\u0090\b\t\u0001\t\u0003\t\u0093\b\t\u0003\t\u0095\b"+
		"\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u009e\b\u000b\n\u000b\f\u000b\u00a1\t\u000b\u0003\u000b\u00a3"+
		"\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00a8\b\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0005\f\u00b4\b\f\n\f\f\f\u00b7\t\f\u0003\f\u00b9\b\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u00c3"+
		"\b\f\n\f\f\f\u00c6\t\f\u0003\f\u00c8\b\f\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u00cd\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003"+
		"\r\u00d6\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0003\r\u00e2\b\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e"+
		"\u00ec\b\u000e\n\u000e\f\u000e\u00ef\t\u000e\u0003\u000e\u00f1\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0103\b\u0011"+
		"\u0001\u0012\u0001\u0012\u0005\u0012\u0107\b\u0012\n\u0012\f\u0012\u010a"+
		"\t\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0004\u0013\u0115\b\u0013\u000b"+
		"\u0013\f\u0013\u0116\u0003\u0013\u0119\b\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0005\u0013\u0125\b\u0013\n\u0013\f\u0013\u0128"+
		"\t\u0013\u0003\u0013\u012a\b\u0013\u0001\u0014\u0001\u0014\u0005\u0014"+
		"\u012e\b\u0014\n\u0014\f\u0014\u0131\t\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0138\b\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0005"+
		"\u001b\u014d\b\u001b\n\u001b\f\u001b\u0150\t\u001b\u0001\u001b\u0005\u001b"+
		"\u0153\b\u001b\n\u001b\f\u001b\u0156\t\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0000\u0000\u001c\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.0246\u0000\u0002\u0001\u0000"+
		"\u0015\u0016\u0002\u0000\u0018\u0018\u001b\u001b\u016e\u00008\u0001\u0000"+
		"\u0000\u0000\u0002:\u0001\u0000\u0000\u0000\u0004<\u0001\u0000\u0000\u0000"+
		"\u0006F\u0001\u0000\u0000\u0000\bH\u0001\u0000\u0000\u0000\nW\u0001\u0000"+
		"\u0000\u0000\ff\u0001\u0000\u0000\u0000\u000el\u0001\u0000\u0000\u0000"+
		"\u0010}\u0001\u0000\u0000\u0000\u0012\u0094\u0001\u0000\u0000\u0000\u0014"+
		"\u0096\u0001\u0000\u0000\u0000\u0016\u00a7\u0001\u0000\u0000\u0000\u0018"+
		"\u00cc\u0001\u0000\u0000\u0000\u001a\u00e1\u0001\u0000\u0000\u0000\u001c"+
		"\u00f0\u0001\u0000\u0000\u0000\u001e\u00f2\u0001\u0000\u0000\u0000 \u00f7"+
		"\u0001\u0000\u0000\u0000\"\u0102\u0001\u0000\u0000\u0000$\u0104\u0001"+
		"\u0000\u0000\u0000&\u0129\u0001\u0000\u0000\u0000(\u012b\u0001\u0000\u0000"+
		"\u0000*\u0132\u0001\u0000\u0000\u0000,\u013b\u0001\u0000\u0000\u0000."+
		"\u013e\u0001\u0000\u0000\u00000\u0141\u0001\u0000\u0000\u00002\u0144\u0001"+
		"\u0000\u0000\u00004\u0147\u0001\u0000\u0000\u00006\u014a\u0001\u0000\u0000"+
		"\u000089\u0005\u001c\u0000\u00009\u0001\u0001\u0000\u0000\u0000:;\u0007"+
		"\u0000\u0000\u0000;\u0003\u0001\u0000\u0000\u0000<=\u0007\u0001\u0000"+
		"\u0000=\u0005\u0001\u0000\u0000\u0000>G\u0005\u0001\u0000\u0000?G\u0005"+
		"\u0004\u0000\u0000@G\u0005\u0002\u0000\u0000AG\u0005\u0003\u0000\u0000"+
		"BG\u0005\u0005\u0000\u0000CG\u0005\u0006\u0000\u0000DG\u0005\b\u0000\u0000"+
		"EG\u0001\u0000\u0000\u0000F>\u0001\u0000\u0000\u0000F?\u0001\u0000\u0000"+
		"\u0000F@\u0001\u0000\u0000\u0000FA\u0001\u0000\u0000\u0000FB\u0001\u0000"+
		"\u0000\u0000FC\u0001\u0000\u0000\u0000FD\u0001\u0000\u0000\u0000FE\u0001"+
		"\u0000\u0000\u0000G\u0007\u0001\u0000\u0000\u0000HI\u0005\u0010\u0000"+
		"\u0000IJ\u0003(\u0014\u0000JK\u0005\u0011\u0000\u0000K\t\u0001\u0000\u0000"+
		"\u0000LM\u0003\u0014\n\u0000MN\u0005\u0013\u0000\u0000NS\u0003\u0006\u0003"+
		"\u0000OP\u0005\u0012\u0000\u0000PR\u0003\u0006\u0003\u0000QO\u0001\u0000"+
		"\u0000\u0000RU\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000\u0000ST\u0001"+
		"\u0000\u0000\u0000TX\u0001\u0000\u0000\u0000US\u0001\u0000\u0000\u0000"+
		"VX\u0003\u0014\n\u0000WL\u0001\u0000\u0000\u0000WV\u0001\u0000\u0000\u0000"+
		"X\u000b\u0001\u0000\u0000\u0000Yg\u0001\u0000\u0000\u0000Zc\u0005\u000e"+
		"\u0000\u0000[`\u0003\n\u0005\u0000\\]\u0005\u0012\u0000\u0000]_\u0003"+
		"\n\u0005\u0000^\\\u0001\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000`^"+
		"\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000ad\u0001\u0000\u0000"+
		"\u0000b`\u0001\u0000\u0000\u0000c[\u0001\u0000\u0000\u0000cd\u0001\u0000"+
		"\u0000\u0000de\u0001\u0000\u0000\u0000eg\u0005\u000f\u0000\u0000fY\u0001"+
		"\u0000\u0000\u0000fZ\u0001\u0000\u0000\u0000g\r\u0001\u0000\u0000\u0000"+
		"hm\u0003\u001e\u000f\u0000ij\u0003\u0006\u0003\u0000jk\u0003\u0010\b\u0000"+
		"km\u0001\u0000\u0000\u0000lh\u0001\u0000\u0000\u0000li\u0001\u0000\u0000"+
		"\u0000m\u000f\u0001\u0000\u0000\u0000ns\u0003\u0014\n\u0000op\u0005\u0012"+
		"\u0000\u0000pr\u0003\u0014\n\u0000qo\u0001\u0000\u0000\u0000ru\u0001\u0000"+
		"\u0000\u0000sq\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000tw\u0001"+
		"\u0000\u0000\u0000us\u0001\u0000\u0000\u0000vn\u0001\u0000\u0000\u0000"+
		"vw\u0001\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000xy\u0005\f\u0000\u0000"+
		"yz\u0003\u0012\t\u0000z{\u0005\r\u0000\u0000{~\u0001\u0000\u0000\u0000"+
		"|~\u0003\u0014\n\u0000}v\u0001\u0000\u0000\u0000}|\u0001\u0000\u0000\u0000"+
		"~\u0011\u0001\u0000\u0000\u0000\u007f\u0095\u0001\u0000\u0000\u0000\u0080"+
		"\u0082\u0005\u0017\u0000\u0000\u0081\u0080\u0001\u0000\u0000\u0000\u0081"+
		"\u0082\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083"+
		"\u0095\u0003\u0016\u000b\u0000\u0084\u0086\u0005\u0017\u0000\u0000\u0085"+
		"\u0084\u0001\u0000\u0000\u0000\u0085\u0086\u0001\u0000\u0000\u0000\u0086"+
		"\u008f\u0001\u0000\u0000\u0000\u0087\u008c\u0003\u0018\f\u0000\u0088\u0089"+
		"\u0005\u0012\u0000\u0000\u0089\u008b\u0003\u0018\f\u0000\u008a\u0088\u0001"+
		"\u0000\u0000\u0000\u008b\u008e\u0001\u0000\u0000\u0000\u008c\u008a\u0001"+
		"\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000\u008d\u0090\u0001"+
		"\u0000\u0000\u0000\u008e\u008c\u0001\u0000\u0000\u0000\u008f\u0087\u0001"+
		"\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u0092\u0001"+
		"\u0000\u0000\u0000\u0091\u0093\u0005\u0012\u0000\u0000\u0092\u0091\u0001"+
		"\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093\u0095\u0001"+
		"\u0000\u0000\u0000\u0094\u007f\u0001\u0000\u0000\u0000\u0094\u0081\u0001"+
		"\u0000\u0000\u0000\u0094\u0085\u0001\u0000\u0000\u0000\u0095\u0013\u0001"+
		"\u0000\u0000\u0000\u0096\u0097\u0003\u0006\u0003\u0000\u0097\u0098\u0003"+
		"\u0000\u0000\u0000\u0098\u0099\u0003\f\u0006\u0000\u0099\u0015\u0001\u0000"+
		"\u0000\u0000\u009a\u009f\u0003\u0002\u0001\u0000\u009b\u009c\u0005\u0012"+
		"\u0000\u0000\u009c\u009e\u0003\u0002\u0001\u0000\u009d\u009b\u0001\u0000"+
		"\u0000\u0000\u009e\u00a1\u0001\u0000\u0000\u0000\u009f\u009d\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a3\u0001\u0000"+
		"\u0000\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2\u009a\u0001\u0000"+
		"\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000"+
		"\u0000\u0000\u00a4\u00a5\u0005\u0014\u0000\u0000\u00a5\u00a8\u0003(\u0014"+
		"\u0000\u00a6\u00a8\u0003(\u0014\u0000\u00a7\u00a2\u0001\u0000\u0000\u0000"+
		"\u00a7\u00a6\u0001\u0000\u0000\u0000\u00a8\u0017\u0001\u0000\u0000\u0000"+
		"\u00a9\u00cd\u0003\u001a\r\u0000\u00aa\u00ab\u0003\u001a\r\u0000\u00ab"+
		"\u00ac\u0005\u0014\u0000\u0000\u00ac\u00ad\u0003(\u0014\u0000\u00ad\u00cd"+
		"\u0001\u0000\u0000\u0000\u00ae\u00af\u0003\u0004\u0002\u0000\u00af\u00b8"+
		"\u0005\u0010\u0000\u0000\u00b0\u00b5\u0003\u0002\u0001\u0000\u00b1\u00b2"+
		"\u0005\u0012\u0000\u0000\u00b2\u00b4\u0003\u0002\u0001\u0000\u00b3\u00b1"+
		"\u0001\u0000\u0000\u0000\u00b4\u00b7\u0001\u0000\u0000\u0000\u00b5\u00b3"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6\u00b9"+
		"\u0001\u0000\u0000\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b8\u00b0"+
		"\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000\u00b9\u00ba"+
		"\u0001\u0000\u0000\u0000\u00ba\u00bb\u0005\u0011\u0000\u0000\u00bb\u00bc"+
		"\u0005\u0014\u0000\u0000\u00bc\u00bd\u0003(\u0014\u0000\u00bd\u00cd\u0001"+
		"\u0000\u0000\u0000\u00be\u00c7\u0003\u0004\u0002\u0000\u00bf\u00c4\u0003"+
		"\u0002\u0001\u0000\u00c0\u00c1\u0005\u0012\u0000\u0000\u00c1\u00c3\u0003"+
		"\u0002\u0001\u0000\u00c2\u00c0\u0001\u0000\u0000\u0000\u00c3\u00c6\u0001"+
		"\u0000\u0000\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000\u00c4\u00c5\u0001"+
		"\u0000\u0000\u0000\u00c5\u00c8\u0001\u0000\u0000\u0000\u00c6\u00c4\u0001"+
		"\u0000\u0000\u0000\u00c7\u00bf\u0001\u0000\u0000\u0000\u00c7\u00c8\u0001"+
		"\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000\u00c9\u00ca\u0005"+
		"\u0014\u0000\u0000\u00ca\u00cb\u0003(\u0014\u0000\u00cb\u00cd\u0001\u0000"+
		"\u0000\u0000\u00cc\u00a9\u0001\u0000\u0000\u0000\u00cc\u00aa\u0001\u0000"+
		"\u0000\u0000\u00cc\u00ae\u0001\u0000\u0000\u0000\u00cc\u00be\u0001\u0000"+
		"\u0000\u0000\u00cd\u0019\u0001\u0000\u0000\u0000\u00ce\u00cf\u0003\u0006"+
		"\u0003\u0000\u00cf\u00d0\u0003\u0004\u0002\u0000\u00d0\u00d5\u0003\f\u0006"+
		"\u0000\u00d1\u00d2\u0005\u0010\u0000\u0000\u00d2\u00d3\u0003\u001c\u000e"+
		"\u0000\u00d3\u00d4\u0005\u0011\u0000\u0000\u00d4\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d5\u00d1\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005\u0013\u0000"+
		"\u0000\u00d8\u00d9\u0003\u0014\n\u0000\u00d9\u00e2\u0001\u0000\u0000\u0000"+
		"\u00da\u00db\u0003\u0006\u0003\u0000\u00db\u00dc\u0003\u0004\u0002\u0000"+
		"\u00dc\u00dd\u0003\f\u0006\u0000\u00dd\u00de\u0003\u001c\u000e\u0000\u00de"+
		"\u00df\u0005\u0013\u0000\u0000\u00df\u00e0\u0003\u0014\n\u0000\u00e0\u00e2"+
		"\u0001\u0000\u0000\u0000\u00e1\u00ce\u0001\u0000\u0000\u0000\u00e1\u00da"+
		"\u0001\u0000\u0000\u0000\u00e2\u001b\u0001\u0000\u0000\u0000\u00e3\u00e4"+
		"\u0003\u0002\u0001\u0000\u00e4\u00e5\u0005\u0013\u0000\u0000\u00e5\u00ed"+
		"\u0003\u0014\n\u0000\u00e6\u00e7\u0005\u0012\u0000\u0000\u00e7\u00e8\u0003"+
		"\u0002\u0001\u0000\u00e8\u00e9\u0005\u0013\u0000\u0000\u00e9\u00ea\u0003"+
		"\u0014\n\u0000\u00ea\u00ec\u0001\u0000\u0000\u0000\u00eb\u00e6\u0001\u0000"+
		"\u0000\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001\u0000"+
		"\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00f1\u0001\u0000"+
		"\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00e3\u0001\u0000"+
		"\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000\u00f1\u001d\u0001\u0000"+
		"\u0000\u0000\u00f2\u00f3\u0003\u0000\u0000\u0000\u00f3\u00f4\u0003\f\u0006"+
		"\u0000\u00f4\u00f5\u0005\u0013\u0000\u0000\u00f5\u00f6\u0003\u0010\b\u0000"+
		"\u00f6\u001f\u0001\u0000\u0000\u0000\u00f7\u00f8\u0005\n\u0000\u0000\u00f8"+
		"\u00f9\u0003\u0000\u0000\u0000\u00f9\u00fa\u0003\f\u0006\u0000\u00fa\u00fb"+
		"\u0005\u000b\u0000\u0000\u00fb\u00fc\u0003\u0000\u0000\u0000\u00fc\u00fd"+
		"\u0003\f\u0006\u0000\u00fd\u00fe\u0005\u0012\u0000\u0000\u00fe!\u0001"+
		"\u0000\u0000\u0000\u00ff\u0103\u0003\u0002\u0001\u0000\u0100\u0103\u0003"+
		"\b\u0004\u0000\u0101\u0103\u0003\u000e\u0007\u0000\u0102\u00ff\u0001\u0000"+
		"\u0000\u0000\u0102\u0100\u0001\u0000\u0000\u0000\u0102\u0101\u0001\u0000"+
		"\u0000\u0000\u0103#\u0001\u0000\u0000\u0000\u0104\u0108\u0003\"\u0011"+
		"\u0000\u0105\u0107\u0003&\u0013\u0000\u0106\u0105\u0001\u0000\u0000\u0000"+
		"\u0107\u010a\u0001\u0000\u0000\u0000\u0108\u0106\u0001\u0000\u0000\u0000"+
		"\u0108\u0109\u0001\u0000\u0000\u0000\u0109%\u0001\u0000\u0000\u0000\u010a"+
		"\u0108\u0001\u0000\u0000\u0000\u010b\u010c\u0003\u0004\u0002\u0000\u010c"+
		"\u010d\u0003\f\u0006\u0000\u010d\u012a\u0001\u0000\u0000\u0000\u010e\u010f"+
		"\u0003\u0004\u0002\u0000\u010f\u0110\u0003\f\u0006\u0000\u0110\u0118\u0005"+
		"\u0010\u0000\u0000\u0111\u0114\u0003(\u0014\u0000\u0112\u0113\u0005\u0012"+
		"\u0000\u0000\u0113\u0115\u0003(\u0014\u0000\u0114\u0112\u0001\u0000\u0000"+
		"\u0000\u0115\u0116\u0001\u0000\u0000\u0000\u0116\u0114\u0001\u0000\u0000"+
		"\u0000\u0116\u0117\u0001\u0000\u0000\u0000\u0117\u0119\u0001\u0000\u0000"+
		"\u0000\u0118\u0111\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000\u0000"+
		"\u0000\u0119\u011a\u0001\u0000\u0000\u0000\u011a\u011b\u0005\u0011\u0000"+
		"\u0000\u011b\u012a\u0001\u0000\u0000\u0000\u011c\u011d\u0003\u0004\u0002"+
		"\u0000\u011d\u011e\u0003\f\u0006\u0000\u011e\u011f\u0005\u0010\u0000\u0000"+
		"\u011f\u0120\u0003\u0002\u0001\u0000\u0120\u0121\u0005\t\u0000\u0000\u0121"+
		"\u0122\u0003(\u0014\u0000\u0122\u0126\u0005\u0011\u0000\u0000\u0123\u0125"+
		"\u0003*\u0015\u0000\u0124\u0123\u0001\u0000\u0000\u0000\u0125\u0128\u0001"+
		"\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000\u0126\u0127\u0001"+
		"\u0000\u0000\u0000\u0127\u012a\u0001\u0000\u0000\u0000\u0128\u0126\u0001"+
		"\u0000\u0000\u0000\u0129\u010b\u0001\u0000\u0000\u0000\u0129\u010e\u0001"+
		"\u0000\u0000\u0000\u0129\u011c\u0001\u0000\u0000\u0000\u012a\'\u0001\u0000"+
		"\u0000\u0000\u012b\u012f\u0003$\u0012\u0000\u012c\u012e\u0003*\u0015\u0000"+
		"\u012d\u012c\u0001\u0000\u0000\u0000\u012e\u0131\u0001\u0000\u0000\u0000"+
		"\u012f\u012d\u0001\u0000\u0000\u0000\u012f\u0130\u0001\u0000\u0000\u0000"+
		"\u0130)\u0001\u0000\u0000\u0000\u0131\u012f\u0001\u0000\u0000\u0000\u0132"+
		"\u0133\u0003\u0004\u0002\u0000\u0133\u0137\u0003\f\u0006\u0000\u0134\u0135"+
		"\u0003\u0002\u0001\u0000\u0135\u0136\u0005\t\u0000\u0000\u0136\u0138\u0001"+
		"\u0000\u0000\u0000\u0137\u0134\u0001\u0000\u0000\u0000\u0137\u0138\u0001"+
		"\u0000\u0000\u0000\u0138\u0139\u0001\u0000\u0000\u0000\u0139\u013a\u0003"+
		"$\u0012\u0000\u013a+\u0001\u0000\u0000\u0000\u013b\u013c\u0003(\u0014"+
		"\u0000\u013c\u013d\u0005\u0000\u0000\u0001\u013d-\u0001\u0000\u0000\u0000"+
		"\u013e\u013f\u0003\u0002\u0001\u0000\u013f\u0140\u0005\u0000\u0000\u0001"+
		"\u0140/\u0001\u0000\u0000\u0000\u0141\u0142\u0003\u0004\u0002\u0000\u0142"+
		"\u0143\u0005\u0000\u0000\u0001\u01431\u0001\u0000\u0000\u0000\u0144\u0145"+
		"\u0003\u0000\u0000\u0000\u0145\u0146\u0005\u0000\u0000\u0001\u01463\u0001"+
		"\u0000\u0000\u0000\u0147\u0148\u0003\u0014\n\u0000\u0148\u0149\u0005\u0000"+
		"\u0000\u0001\u01495\u0001\u0000\u0000\u0000\u014a\u014e\u0005\u001e\u0000"+
		"\u0000\u014b\u014d\u0003 \u0010\u0000\u014c\u014b\u0001\u0000\u0000\u0000"+
		"\u014d\u0150\u0001\u0000\u0000\u0000\u014e\u014c\u0001\u0000\u0000\u0000"+
		"\u014e\u014f\u0001\u0000\u0000\u0000\u014f\u0154\u0001\u0000\u0000\u0000"+
		"\u0150\u014e\u0001\u0000\u0000\u0000\u0151\u0153\u0003\u001e\u000f\u0000"+
		"\u0152\u0151\u0001\u0000\u0000\u0000\u0153\u0156\u0001\u0000\u0000\u0000"+
		"\u0154\u0152\u0001\u0000\u0000\u0000\u0154\u0155\u0001\u0000\u0000\u0000"+
		"\u0155\u0157\u0001\u0000\u0000\u0000\u0156\u0154\u0001\u0000\u0000\u0000"+
		"\u0157\u0158\u0005\u0000\u0000\u0001\u01587\u0001\u0000\u0000\u0000&F"+
		"SW`cflsv}\u0081\u0085\u008c\u008f\u0092\u0094\u009f\u00a2\u00a7\u00b5"+
		"\u00b8\u00c4\u00c7\u00cc\u00d5\u00e1\u00ed\u00f0\u0102\u0108\u0116\u0118"+
		"\u0126\u0129\u012f\u0137\u014e\u0154";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}