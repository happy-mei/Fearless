// Generated from C:\Users\sonta\Documents\GitHub\Fearless\grammar\antlrGrammars\Fearless.g4 by ANTLR 4.12.0
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
		Mut=1, MutH=2, Read=3, ReadImm=4, ReadH=5, Iso=6, Imm=7, Eq=8, Alias=9, 
		As=10, ColonColon=11, OC=12, CC=13, OS=14, CS=15, OR=16, CR=17, Comma=18, 
		Colon=19, Arrow=20, Underscore=21, X=22, SelfX=23, MName=24, CCMName=25, 
		FStringMulti=26, BlockComment=27, LineComment=28, SysInM=29, FullCN=30, 
		Whitespace=31, Pack=32;
	public static final int
		RULE_fullCN = 0, RULE_x = 1, RULE_m = 2, RULE_mdf = 3, RULE_roundE = 4, 
		RULE_genDecl = 5, RULE_mGen = 6, RULE_actualGen = 7, RULE_topDec = 8, 
		RULE_lambda = 9, RULE_bblock = 10, RULE_t = 11, RULE_singleM = 12, RULE_meth = 13, 
		RULE_sig = 14, RULE_gamma = 15, RULE_alias = 16, RULE_fStringMulti = 17, 
		RULE_atomE = 18, RULE_e = 19, RULE_pOp = 20, RULE_nudeE = 21, RULE_nudeX = 22, 
		RULE_nudeM = 23, RULE_nudeFullCN = 24, RULE_nudeT = 25, RULE_nudeProgram = 26;
	private static String[] makeRuleNames() {
		return new String[] {
			"fullCN", "x", "m", "mdf", "roundE", "genDecl", "mGen", "actualGen", 
			"topDec", "lambda", "bblock", "t", "singleM", "meth", "sig", "gamma", 
			"alias", "fStringMulti", "atomE", "e", "pOp", "nudeE", "nudeX", "nudeM", 
			"nudeFullCN", "nudeT", "nudeProgram"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'mut'", "'mutH'", "'read'", "'read/imm'", "'readH'", "'iso'", 
			"'imm'", "'='", "'alias'", "'as'", "'::'", "'{'", "'}'", "'['", "']'", 
			"'('", "')'", "','", "':'", "'->'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Mut", "MutH", "Read", "ReadImm", "ReadH", "Iso", "Imm", "Eq", 
			"Alias", "As", "ColonColon", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", 
			"Colon", "Arrow", "Underscore", "X", "SelfX", "MName", "CCMName", "FStringMulti", 
			"BlockComment", "LineComment", "SysInM", "FullCN", "Whitespace", "Pack"
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
			setState(54);
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
			setState(56);
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
			setState(58);
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
		public TerminalNode ReadH() { return getToken(FearlessParser.ReadH, 0); }
		public TerminalNode MutH() { return getToken(FearlessParser.MutH, 0); }
		public TerminalNode ReadImm() { return getToken(FearlessParser.ReadImm, 0); }
		public TerminalNode Read() { return getToken(FearlessParser.Read, 0); }
		public TerminalNode Iso() { return getToken(FearlessParser.Iso, 0); }
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
			setState(68);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Mut:
				enterOuterAlt(_localctx, 1);
				{
				setState(60);
				match(Mut);
				}
				break;
			case ReadH:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				match(ReadH);
				}
				break;
			case MutH:
				enterOuterAlt(_localctx, 3);
				{
				setState(62);
				match(MutH);
				}
				break;
			case ReadImm:
				enterOuterAlt(_localctx, 4);
				{
				setState(63);
				match(ReadImm);
				}
				break;
			case Read:
				enterOuterAlt(_localctx, 5);
				{
				setState(64);
				match(Read);
				}
				break;
			case Iso:
				enterOuterAlt(_localctx, 6);
				{
				setState(65);
				match(Iso);
				}
				break;
			case Imm:
				enterOuterAlt(_localctx, 7);
				{
				setState(66);
				match(Imm);
				}
				break;
			case OC:
			case CS:
			case Comma:
			case MName:
			case SysInM:
			case FullCN:
				enterOuterAlt(_localctx, 8);
				{
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
			setState(70);
			match(OR);
			setState(71);
			e();
			setState(72);
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
		public FullCNContext fullCN() {
			return getRuleContext(FullCNContext.class,0);
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
		public TerminalNode SysInM() { return getToken(FearlessParser.SysInM, 0); }
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
			setState(90);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				fullCN();
				setState(75);
				match(Colon);
				setState(76);
				mdf();
				setState(81);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(77);
						match(Comma);
						setState(78);
						mdf();
						}
						} 
					}
					setState(83);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(84);
				fullCN();
				setState(88);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case Colon:
					{
					setState(85);
					match(Colon);
					setState(86);
					match(SysInM);
					}
					break;
				case CS:
				case Comma:
					{
					}
					break;
				default:
					throw new NoViableAltException(this);
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
			setState(105);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OR:
			case Colon:
			case Underscore:
			case X:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case OS:
				enterOuterAlt(_localctx, 2);
				{
				setState(93);
				match(OS);
				setState(102);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FullCN) {
					{
					setState(94);
					genDecl();
					setState(99);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(95);
						match(Comma);
						setState(96);
						genDecl();
						}
						}
						setState(101);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(104);
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
	public static class ActualGenContext extends ParserRuleContext {
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
		public ActualGenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_actualGen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterActualGen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitActualGen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitActualGen(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ActualGenContext actualGen() throws RecognitionException {
		ActualGenContext _localctx = new ActualGenContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_actualGen);
		int _la;
		try {
			setState(120);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
			case Mut:
			case MutH:
			case Read:
			case ReadImm:
			case ReadH:
			case Iso:
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
			case FStringMulti:
			case SysInM:
			case FullCN:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case OS:
				enterOuterAlt(_localctx, 2);
				{
				setState(108);
				match(OS);
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1073742078L) != 0)) {
					{
					setState(109);
					t();
					setState(114);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(110);
						match(Comma);
						setState(111);
						t();
						}
						}
						setState(116);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(119);
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
	public static class TopDecContext extends ParserRuleContext {
		public FullCNContext fullCN() {
			return getRuleContext(FullCNContext.class,0);
		}
		public MGenContext mGen() {
			return getRuleContext(MGenContext.class,0);
		}
		public TerminalNode Colon() { return getToken(FearlessParser.Colon, 0); }
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
		enterRule(_localctx, 16, RULE_topDec);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			fullCN();
			setState(123);
			mGen();
			setState(124);
			match(Colon);
			setState(136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1073742078L) != 0)) {
				{
				setState(125);
				t();
				setState(130);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(126);
						match(Comma);
						setState(127);
						t();
						}
						} 
					}
					setState(132);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				}
				setState(134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(133);
					match(Comma);
					}
				}

				}
			}

			setState(138);
			match(OC);
			setState(139);
			bblock();
			setState(140);
			match(CC);
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
		public TopDecContext topDec() {
			return getRuleContext(TopDecContext.class,0);
		}
		public TerminalNode OC() { return getToken(FearlessParser.OC, 0); }
		public BblockContext bblock() {
			return getRuleContext(BblockContext.class,0);
		}
		public TerminalNode CC() { return getToken(FearlessParser.CC, 0); }
		public TContext t() {
			return getRuleContext(TContext.class,0);
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
		enterRule(_localctx, 18, RULE_lambda);
		try {
			setState(154);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(142);
				mdf();
				setState(143);
				topDec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(147);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(145);
					t();
					}
					break;
				case 2:
					{
					setState(146);
					mdf();
					}
					break;
				}
				setState(149);
				match(OC);
				setState(150);
				bblock();
				setState(151);
				match(CC);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(153);
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
		public ActualGenContext actualGen() {
			return getRuleContext(ActualGenContext.class,0);
		}
		public TerminalNode CCMName() { return getToken(FearlessParser.CCMName, 0); }
		public TerminalNode SysInM() { return getToken(FearlessParser.SysInM, 0); }
		public List<POpContext> pOp() {
			return getRuleContexts(POpContext.class);
		}
		public POpContext pOp(int i) {
			return getRuleContext(POpContext.class,i);
		}
		public TerminalNode OR() { return getToken(FearlessParser.OR, 0); }
		public TerminalNode CR() { return getToken(FearlessParser.CR, 0); }
		public List<EContext> e() {
			return getRuleContexts(EContext.class);
		}
		public EContext e(int i) {
			return getRuleContext(EContext.class,i);
		}
		public AtomEContext atomE() {
			return getRuleContext(AtomEContext.class,0);
		}
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public TerminalNode Eq() { return getToken(FearlessParser.Eq, 0); }
		public TerminalNode ColonColon() { return getToken(FearlessParser.ColonColon, 0); }
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
		enterRule(_localctx, 20, RULE_bblock);
		int _la;
		try {
			int _alt;
			setState(224);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(158);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SelfX) {
					{
					setState(157);
					match(SelfX);
					}
				}

				setState(160);
				singleM();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(162);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SelfX) {
					{
					setState(161);
					match(SelfX);
					}
				}

				setState(172);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 553648382L) != 0)) {
					{
					setState(164);
					meth();
					setState(169);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(165);
							match(Comma);
							setState(166);
							meth();
							}
							} 
						}
						setState(171);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
					}
					}
				}

				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(174);
					match(Comma);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(177);
				_la = _input.LA(1);
				if ( !(_la==CCMName || _la==SysInM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(178);
				actualGen();
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==MName || _la==SysInM) {
					{
					{
					setState(179);
					pOp();
					}
					}
					setState(184);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(185);
				_la = _input.LA(1);
				if ( !(_la==CCMName || _la==SysInM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(186);
				actualGen();
				setState(187);
				match(OR);
				setState(195);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1147212030L) != 0)) {
					{
					setState(188);
					e();
					setState(191); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(189);
						match(Comma);
						setState(190);
						e();
						}
						}
						setState(193); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(197);
				match(CR);
				setState(201);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==MName || _la==SysInM) {
					{
					{
					setState(198);
					pOp();
					}
					}
					setState(203);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(204);
				_la = _input.LA(1);
				if ( !(_la==CCMName || _la==SysInM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(205);
				actualGen();
				setState(206);
				atomE();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(208);
				_la = _input.LA(1);
				if ( !(_la==CCMName || _la==SysInM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(209);
				actualGen();
				setState(210);
				atomE();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(212);
				_la = _input.LA(1);
				if ( !(_la==CCMName || _la==SysInM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(213);
				actualGen();
				setState(214);
				x();
				setState(215);
				match(Eq);
				setState(216);
				atomE();
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==MName || _la==SysInM) {
					{
					{
					setState(217);
					pOp();
					}
					}
					setState(222);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(223);
				match(ColonColon);
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
		public ActualGenContext actualGen() {
			return getRuleContext(ActualGenContext.class,0);
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
		enterRule(_localctx, 22, RULE_t);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			mdf();
			setState(227);
			fullCN();
			setState(228);
			actualGen();
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
		enterRule(_localctx, 24, RULE_singleM);
		int _la;
		try {
			setState(243);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(238);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(230);
					x();
					setState(235);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(231);
						match(Comma);
						setState(232);
						x();
						}
						}
						setState(237);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(240);
				match(Arrow);
				setState(241);
				e();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(242);
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
		enterRule(_localctx, 26, RULE_meth);
		int _la;
		try {
			setState(280);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(245);
				sig();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(246);
				sig();
				setState(247);
				match(Arrow);
				setState(248);
				e();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(250);
				m();
				setState(251);
				match(OR);
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(252);
					x();
					setState(257);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(253);
						match(Comma);
						setState(254);
						x();
						}
						}
						setState(259);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(262);
				match(CR);
				setState(263);
				match(Arrow);
				setState(264);
				e();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(266);
				m();
				setState(275);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Underscore || _la==X) {
					{
					setState(267);
					x();
					setState(272);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Comma) {
						{
						{
						setState(268);
						match(Comma);
						setState(269);
						x();
						}
						}
						setState(274);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(277);
				match(Arrow);
				setState(278);
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
		enterRule(_localctx, 28, RULE_sig);
		int _la;
		try {
			setState(301);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				mdf();
				setState(283);
				m();
				setState(284);
				mGen();
				setState(289);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(285);
					match(OR);
					setState(286);
					gamma();
					setState(287);
					match(CR);
					}
				}

				setState(291);
				match(Colon);
				setState(292);
				t();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(294);
				mdf();
				setState(295);
				m();
				setState(296);
				mGen();
				setState(297);
				gamma();
				setState(298);
				match(Colon);
				setState(299);
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
		enterRule(_localctx, 30, RULE_gamma);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Underscore || _la==X) {
				{
				setState(303);
				x();
				setState(304);
				match(Colon);
				setState(305);
				t();
				setState(313);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==Comma) {
					{
					{
					setState(306);
					match(Comma);
					setState(307);
					x();
					setState(308);
					match(Colon);
					setState(309);
					t();
					}
					}
					setState(315);
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
	public static class AliasContext extends ParserRuleContext {
		public TerminalNode Alias() { return getToken(FearlessParser.Alias, 0); }
		public List<FullCNContext> fullCN() {
			return getRuleContexts(FullCNContext.class);
		}
		public FullCNContext fullCN(int i) {
			return getRuleContext(FullCNContext.class,i);
		}
		public ActualGenContext actualGen() {
			return getRuleContext(ActualGenContext.class,0);
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
			setState(318);
			match(Alias);
			setState(319);
			fullCN();
			setState(320);
			actualGen();
			setState(321);
			match(As);
			setState(322);
			fullCN();
			setState(323);
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
	public static class FStringMultiContext extends ParserRuleContext {
		public TerminalNode FStringMulti() { return getToken(FearlessParser.FStringMulti, 0); }
		public FStringMultiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fStringMulti; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).enterFStringMulti(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FearlessListener ) ((FearlessListener)listener).exitFStringMulti(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FearlessVisitor ) return ((FearlessVisitor<? extends T>)visitor).visitFStringMulti(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FStringMultiContext fStringMulti() throws RecognitionException {
		FStringMultiContext _localctx = new FStringMultiContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_fStringMulti);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(325);
			match(FStringMulti);
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
		public FStringMultiContext fStringMulti() {
			return getRuleContext(FStringMultiContext.class,0);
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
		enterRule(_localctx, 36, RULE_atomE);
		try {
			setState(331);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Underscore:
			case X:
				enterOuterAlt(_localctx, 1);
				{
				setState(327);
				x();
				}
				break;
			case OR:
				enterOuterAlt(_localctx, 2);
				{
				setState(328);
				roundE();
				}
				break;
			case Mut:
			case MutH:
			case Read:
			case ReadImm:
			case ReadH:
			case Iso:
			case Imm:
			case OC:
			case FullCN:
				enterOuterAlt(_localctx, 3);
				{
				setState(329);
				lambda();
				}
				break;
			case FStringMulti:
				enterOuterAlt(_localctx, 4);
				{
				setState(330);
				fStringMulti();
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
	public static class EContext extends ParserRuleContext {
		public AtomEContext atomE() {
			return getRuleContext(AtomEContext.class,0);
		}
		public List<POpContext> pOp() {
			return getRuleContexts(POpContext.class);
		}
		public POpContext pOp(int i) {
			return getRuleContext(POpContext.class,i);
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
			setState(333);
			atomE();
			setState(337);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MName || _la==SysInM) {
				{
				{
				setState(334);
				pOp();
				}
				}
				setState(339);
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
	public static class POpContext extends ParserRuleContext {
		public MContext m() {
			return getRuleContext(MContext.class,0);
		}
		public ActualGenContext actualGen() {
			return getRuleContext(ActualGenContext.class,0);
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
		public AtomEContext atomE() {
			return getRuleContext(AtomEContext.class,0);
		}
		public XContext x() {
			return getRuleContext(XContext.class,0);
		}
		public TerminalNode Eq() { return getToken(FearlessParser.Eq, 0); }
		public List<POpContext> pOp() {
			return getRuleContexts(POpContext.class);
		}
		public POpContext pOp(int i) {
			return getRuleContext(POpContext.class,i);
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
		enterRule(_localctx, 40, RULE_pOp);
		int _la;
		try {
			int _alt;
			setState(372);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(340);
				m();
				setState(341);
				actualGen();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(343);
				m();
				setState(344);
				actualGen();
				setState(345);
				match(OR);
				setState(353);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1147212030L) != 0)) {
					{
					setState(346);
					e();
					setState(349); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(347);
						match(Comma);
						setState(348);
						e();
						}
						}
						setState(351); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==Comma );
					}
				}

				setState(355);
				match(CR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(357);
				m();
				setState(358);
				actualGen();
				setState(359);
				atomE();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(361);
				m();
				setState(362);
				actualGen();
				setState(363);
				x();
				setState(364);
				match(Eq);
				setState(365);
				atomE();
				setState(369);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(366);
						pOp();
						}
						} 
					}
					setState(371);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
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
		enterRule(_localctx, 42, RULE_nudeE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			e();
			setState(375);
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
		enterRule(_localctx, 44, RULE_nudeX);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
			x();
			setState(378);
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
		enterRule(_localctx, 46, RULE_nudeM);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(380);
			m();
			setState(381);
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
		enterRule(_localctx, 48, RULE_nudeFullCN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(383);
			fullCN();
			setState(384);
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
		enterRule(_localctx, 50, RULE_nudeT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			t();
			setState(387);
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
		enterRule(_localctx, 52, RULE_nudeProgram);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			match(Pack);
			setState(393);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Alias) {
				{
				{
				setState(390);
				alias();
				}
				}
				setState(395);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FullCN) {
				{
				{
				setState(396);
				topDec();
				}
				}
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(402);
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
		"\u0004\u0001 \u0195\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u0003E\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005"+
		"P\b\u0005\n\u0005\f\u0005S\t\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005Y\b\u0005\u0003\u0005[\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006b\b\u0006\n\u0006"+
		"\f\u0006e\t\u0006\u0003\u0006g\b\u0006\u0001\u0006\u0003\u0006j\b\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007"+
		"q\b\u0007\n\u0007\f\u0007t\t\u0007\u0003\u0007v\b\u0007\u0001\u0007\u0003"+
		"\u0007y\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0005"+
		"\b\u0081\b\b\n\b\f\b\u0084\t\b\u0001\b\u0003\b\u0087\b\b\u0003\b\u0089"+
		"\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u0094\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u009b"+
		"\b\t\u0001\n\u0001\n\u0003\n\u009f\b\n\u0001\n\u0001\n\u0003\n\u00a3\b"+
		"\n\u0001\n\u0001\n\u0001\n\u0005\n\u00a8\b\n\n\n\f\n\u00ab\t\n\u0003\n"+
		"\u00ad\b\n\u0001\n\u0003\n\u00b0\b\n\u0001\n\u0001\n\u0001\n\u0005\n\u00b5"+
		"\b\n\n\n\f\n\u00b8\t\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n"+
		"\u0004\n\u00c0\b\n\u000b\n\f\n\u00c1\u0003\n\u00c4\b\n\u0001\n\u0001\n"+
		"\u0005\n\u00c8\b\n\n\n\f\n\u00cb\t\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0005\n\u00db\b\n\n\n\f\n\u00de\t\n\u0001\n\u0003\n\u00e1\b\n\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005"+
		"\f\u00ea\b\f\n\f\f\f\u00ed\t\f\u0003\f\u00ef\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u00f4\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0005\r\u0100\b\r\n\r\f\r\u0103\t\r\u0003\r"+
		"\u0105\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0005\r\u010f\b\r\n\r\f\r\u0112\t\r\u0003\r\u0114\b\r\u0001\r\u0001"+
		"\r\u0001\r\u0003\r\u0119\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0122\b\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u012e\b\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0005\u000f\u0138\b\u000f\n\u000f\f\u000f\u013b\t\u000f"+
		"\u0003\u000f\u013d\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u014c\b\u0012\u0001\u0013"+
		"\u0001\u0013\u0005\u0013\u0150\b\u0013\n\u0013\f\u0013\u0153\t\u0013\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0004\u0014\u015e\b\u0014\u000b\u0014\f"+
		"\u0014\u015f\u0003\u0014\u0162\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u0170\b\u0014\n\u0014"+
		"\f\u0014\u0173\t\u0014\u0003\u0014\u0175\b\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u001a\u0001\u001a\u0005\u001a\u0188\b\u001a\n\u001a"+
		"\f\u001a\u018b\t\u001a\u0001\u001a\u0005\u001a\u018e\b\u001a\n\u001a\f"+
		"\u001a\u0191\t\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0000\u0000\u001b"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.024\u0000\u0003\u0001\u0000\u0015\u0016\u0002\u0000"+
		"\u0018\u0018\u001d\u001d\u0002\u0000\u0019\u0019\u001d\u001d\u01bb\u0000"+
		"6\u0001\u0000\u0000\u0000\u00028\u0001\u0000\u0000\u0000\u0004:\u0001"+
		"\u0000\u0000\u0000\u0006D\u0001\u0000\u0000\u0000\bF\u0001\u0000\u0000"+
		"\u0000\nZ\u0001\u0000\u0000\u0000\fi\u0001\u0000\u0000\u0000\u000ex\u0001"+
		"\u0000\u0000\u0000\u0010z\u0001\u0000\u0000\u0000\u0012\u009a\u0001\u0000"+
		"\u0000\u0000\u0014\u00e0\u0001\u0000\u0000\u0000\u0016\u00e2\u0001\u0000"+
		"\u0000\u0000\u0018\u00f3\u0001\u0000\u0000\u0000\u001a\u0118\u0001\u0000"+
		"\u0000\u0000\u001c\u012d\u0001\u0000\u0000\u0000\u001e\u013c\u0001\u0000"+
		"\u0000\u0000 \u013e\u0001\u0000\u0000\u0000\"\u0145\u0001\u0000\u0000"+
		"\u0000$\u014b\u0001\u0000\u0000\u0000&\u014d\u0001\u0000\u0000\u0000("+
		"\u0174\u0001\u0000\u0000\u0000*\u0176\u0001\u0000\u0000\u0000,\u0179\u0001"+
		"\u0000\u0000\u0000.\u017c\u0001\u0000\u0000\u00000\u017f\u0001\u0000\u0000"+
		"\u00002\u0182\u0001\u0000\u0000\u00004\u0185\u0001\u0000\u0000\u00006"+
		"7\u0005\u001e\u0000\u00007\u0001\u0001\u0000\u0000\u000089\u0007\u0000"+
		"\u0000\u00009\u0003\u0001\u0000\u0000\u0000:;\u0007\u0001\u0000\u0000"+
		";\u0005\u0001\u0000\u0000\u0000<E\u0005\u0001\u0000\u0000=E\u0005\u0005"+
		"\u0000\u0000>E\u0005\u0002\u0000\u0000?E\u0005\u0004\u0000\u0000@E\u0005"+
		"\u0003\u0000\u0000AE\u0005\u0006\u0000\u0000BE\u0005\u0007\u0000\u0000"+
		"CE\u0001\u0000\u0000\u0000D<\u0001\u0000\u0000\u0000D=\u0001\u0000\u0000"+
		"\u0000D>\u0001\u0000\u0000\u0000D?\u0001\u0000\u0000\u0000D@\u0001\u0000"+
		"\u0000\u0000DA\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000DC\u0001"+
		"\u0000\u0000\u0000E\u0007\u0001\u0000\u0000\u0000FG\u0005\u0010\u0000"+
		"\u0000GH\u0003&\u0013\u0000HI\u0005\u0011\u0000\u0000I\t\u0001\u0000\u0000"+
		"\u0000JK\u0003\u0000\u0000\u0000KL\u0005\u0013\u0000\u0000LQ\u0003\u0006"+
		"\u0003\u0000MN\u0005\u0012\u0000\u0000NP\u0003\u0006\u0003\u0000OM\u0001"+
		"\u0000\u0000\u0000PS\u0001\u0000\u0000\u0000QO\u0001\u0000\u0000\u0000"+
		"QR\u0001\u0000\u0000\u0000R[\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000"+
		"\u0000TX\u0003\u0000\u0000\u0000UV\u0005\u0013\u0000\u0000VY\u0005\u001d"+
		"\u0000\u0000WY\u0001\u0000\u0000\u0000XU\u0001\u0000\u0000\u0000XW\u0001"+
		"\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000ZJ\u0001\u0000\u0000\u0000"+
		"ZT\u0001\u0000\u0000\u0000[\u000b\u0001\u0000\u0000\u0000\\j\u0001\u0000"+
		"\u0000\u0000]f\u0005\u000e\u0000\u0000^c\u0003\n\u0005\u0000_`\u0005\u0012"+
		"\u0000\u0000`b\u0003\n\u0005\u0000a_\u0001\u0000\u0000\u0000be\u0001\u0000"+
		"\u0000\u0000ca\u0001\u0000\u0000\u0000cd\u0001\u0000\u0000\u0000dg\u0001"+
		"\u0000\u0000\u0000ec\u0001\u0000\u0000\u0000f^\u0001\u0000\u0000\u0000"+
		"fg\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000hj\u0005\u000f\u0000"+
		"\u0000i\\\u0001\u0000\u0000\u0000i]\u0001\u0000\u0000\u0000j\r\u0001\u0000"+
		"\u0000\u0000ky\u0001\u0000\u0000\u0000lu\u0005\u000e\u0000\u0000mr\u0003"+
		"\u0016\u000b\u0000no\u0005\u0012\u0000\u0000oq\u0003\u0016\u000b\u0000"+
		"pn\u0001\u0000\u0000\u0000qt\u0001\u0000\u0000\u0000rp\u0001\u0000\u0000"+
		"\u0000rs\u0001\u0000\u0000\u0000sv\u0001\u0000\u0000\u0000tr\u0001\u0000"+
		"\u0000\u0000um\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vw\u0001"+
		"\u0000\u0000\u0000wy\u0005\u000f\u0000\u0000xk\u0001\u0000\u0000\u0000"+
		"xl\u0001\u0000\u0000\u0000y\u000f\u0001\u0000\u0000\u0000z{\u0003\u0000"+
		"\u0000\u0000{|\u0003\f\u0006\u0000|\u0088\u0005\u0013\u0000\u0000}\u0082"+
		"\u0003\u0016\u000b\u0000~\u007f\u0005\u0012\u0000\u0000\u007f\u0081\u0003"+
		"\u0016\u000b\u0000\u0080~\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000"+
		"\u0000\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000"+
		"\u0000\u0000\u0083\u0086\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000"+
		"\u0000\u0000\u0085\u0087\u0005\u0012\u0000\u0000\u0086\u0085\u0001\u0000"+
		"\u0000\u0000\u0086\u0087\u0001\u0000\u0000\u0000\u0087\u0089\u0001\u0000"+
		"\u0000\u0000\u0088}\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000\u0000"+
		"\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u008b\u0005\f\u0000\u0000"+
		"\u008b\u008c\u0003\u0014\n\u0000\u008c\u008d\u0005\r\u0000\u0000\u008d"+
		"\u0011\u0001\u0000\u0000\u0000\u008e\u008f\u0003\u0006\u0003\u0000\u008f"+
		"\u0090\u0003\u0010\b\u0000\u0090\u009b\u0001\u0000\u0000\u0000\u0091\u0094"+
		"\u0003\u0016\u000b\u0000\u0092\u0094\u0003\u0006\u0003\u0000\u0093\u0091"+
		"\u0001\u0000\u0000\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0094\u0095"+
		"\u0001\u0000\u0000\u0000\u0095\u0096\u0005\f\u0000\u0000\u0096\u0097\u0003"+
		"\u0014\n\u0000\u0097\u0098\u0005\r\u0000\u0000\u0098\u009b\u0001\u0000"+
		"\u0000\u0000\u0099\u009b\u0003\u0016\u000b\u0000\u009a\u008e\u0001\u0000"+
		"\u0000\u0000\u009a\u0093\u0001\u0000\u0000\u0000\u009a\u0099\u0001\u0000"+
		"\u0000\u0000\u009b\u0013\u0001\u0000\u0000\u0000\u009c\u00e1\u0001\u0000"+
		"\u0000\u0000\u009d\u009f\u0005\u0017\u0000\u0000\u009e\u009d\u0001\u0000"+
		"\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u00a0\u0001\u0000"+
		"\u0000\u0000\u00a0\u00e1\u0003\u0018\f\u0000\u00a1\u00a3\u0005\u0017\u0000"+
		"\u0000\u00a2\u00a1\u0001\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000"+
		"\u0000\u00a3\u00ac\u0001\u0000\u0000\u0000\u00a4\u00a9\u0003\u001a\r\u0000"+
		"\u00a5\u00a6\u0005\u0012\u0000\u0000\u00a6\u00a8\u0003\u001a\r\u0000\u00a7"+
		"\u00a5\u0001\u0000\u0000\u0000\u00a8\u00ab\u0001\u0000\u0000\u0000\u00a9"+
		"\u00a7\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000\u00aa"+
		"\u00ad\u0001\u0000\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ac"+
		"\u00a4\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad"+
		"\u00af\u0001\u0000\u0000\u0000\u00ae\u00b0\u0005\u0012\u0000\u0000\u00af"+
		"\u00ae\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0"+
		"\u00e1\u0001\u0000\u0000\u0000\u00b1\u00b2\u0007\u0002\u0000\u0000\u00b2"+
		"\u00b6\u0003\u000e\u0007\u0000\u00b3\u00b5\u0003(\u0014\u0000\u00b4\u00b3"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b8\u0001\u0000\u0000\u0000\u00b6\u00b4"+
		"\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7\u00e1"+
		"\u0001\u0000\u0000\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b9\u00ba"+
		"\u0007\u0002\u0000\u0000\u00ba\u00bb\u0003\u000e\u0007\u0000\u00bb\u00c3"+
		"\u0005\u0010\u0000\u0000\u00bc\u00bf\u0003&\u0013\u0000\u00bd\u00be\u0005"+
		"\u0012\u0000\u0000\u00be\u00c0\u0003&\u0013\u0000\u00bf\u00bd\u0001\u0000"+
		"\u0000\u0000\u00c0\u00c1\u0001\u0000\u0000\u0000\u00c1\u00bf\u0001\u0000"+
		"\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2\u00c4\u0001\u0000"+
		"\u0000\u0000\u00c3\u00bc\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001\u0000"+
		"\u0000\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000\u00c5\u00c9\u0005\u0011"+
		"\u0000\u0000\u00c6\u00c8\u0003(\u0014\u0000\u00c7\u00c6\u0001\u0000\u0000"+
		"\u0000\u00c8\u00cb\u0001\u0000\u0000\u0000\u00c9\u00c7\u0001\u0000\u0000"+
		"\u0000\u00c9\u00ca\u0001\u0000\u0000\u0000\u00ca\u00e1\u0001\u0000\u0000"+
		"\u0000\u00cb\u00c9\u0001\u0000\u0000\u0000\u00cc\u00cd\u0007\u0002\u0000"+
		"\u0000\u00cd\u00ce\u0003\u000e\u0007\u0000\u00ce\u00cf\u0003$\u0012\u0000"+
		"\u00cf\u00e1\u0001\u0000\u0000\u0000\u00d0\u00d1\u0007\u0002\u0000\u0000"+
		"\u00d1\u00d2\u0003\u000e\u0007\u0000\u00d2\u00d3\u0003$\u0012\u0000\u00d3"+
		"\u00e1\u0001\u0000\u0000\u0000\u00d4\u00d5\u0007\u0002\u0000\u0000\u00d5"+
		"\u00d6\u0003\u000e\u0007\u0000\u00d6\u00d7\u0003\u0002\u0001\u0000\u00d7"+
		"\u00d8\u0005\b\u0000\u0000\u00d8\u00dc\u0003$\u0012\u0000\u00d9\u00db"+
		"\u0003(\u0014\u0000\u00da\u00d9\u0001\u0000\u0000\u0000\u00db\u00de\u0001"+
		"\u0000\u0000\u0000\u00dc\u00da\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001"+
		"\u0000\u0000\u0000\u00dd\u00e1\u0001\u0000\u0000\u0000\u00de\u00dc\u0001"+
		"\u0000\u0000\u0000\u00df\u00e1\u0005\u000b\u0000\u0000\u00e0\u009c\u0001"+
		"\u0000\u0000\u0000\u00e0\u009e\u0001\u0000\u0000\u0000\u00e0\u00a2\u0001"+
		"\u0000\u0000\u0000\u00e0\u00b1\u0001\u0000\u0000\u0000\u00e0\u00b9\u0001"+
		"\u0000\u0000\u0000\u00e0\u00cc\u0001\u0000\u0000\u0000\u00e0\u00d0\u0001"+
		"\u0000\u0000\u0000\u00e0\u00d4\u0001\u0000\u0000\u0000\u00e0\u00df\u0001"+
		"\u0000\u0000\u0000\u00e1\u0015\u0001\u0000\u0000\u0000\u00e2\u00e3\u0003"+
		"\u0006\u0003\u0000\u00e3\u00e4\u0003\u0000\u0000\u0000\u00e4\u00e5\u0003"+
		"\u000e\u0007\u0000\u00e5\u0017\u0001\u0000\u0000\u0000\u00e6\u00eb\u0003"+
		"\u0002\u0001\u0000\u00e7\u00e8\u0005\u0012\u0000\u0000\u00e8\u00ea\u0003"+
		"\u0002\u0001\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00ea\u00ed\u0001"+
		"\u0000\u0000\u0000\u00eb\u00e9\u0001\u0000\u0000\u0000\u00eb\u00ec\u0001"+
		"\u0000\u0000\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001"+
		"\u0000\u0000\u0000\u00ee\u00e6\u0001\u0000\u0000\u0000\u00ee\u00ef\u0001"+
		"\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u00f1\u0005"+
		"\u0014\u0000\u0000\u00f1\u00f4\u0003&\u0013\u0000\u00f2\u00f4\u0003&\u0013"+
		"\u0000\u00f3\u00ee\u0001\u0000\u0000\u0000\u00f3\u00f2\u0001\u0000\u0000"+
		"\u0000\u00f4\u0019\u0001\u0000\u0000\u0000\u00f5\u0119\u0003\u001c\u000e"+
		"\u0000\u00f6\u00f7\u0003\u001c\u000e\u0000\u00f7\u00f8\u0005\u0014\u0000"+
		"\u0000\u00f8\u00f9\u0003&\u0013\u0000\u00f9\u0119\u0001\u0000\u0000\u0000"+
		"\u00fa\u00fb\u0003\u0004\u0002\u0000\u00fb\u0104\u0005\u0010\u0000\u0000"+
		"\u00fc\u0101\u0003\u0002\u0001\u0000\u00fd\u00fe\u0005\u0012\u0000\u0000"+
		"\u00fe\u0100\u0003\u0002\u0001\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000"+
		"\u0100\u0103\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000"+
		"\u0101\u0102\u0001\u0000\u0000\u0000\u0102\u0105\u0001\u0000\u0000\u0000"+
		"\u0103\u0101\u0001\u0000\u0000\u0000\u0104\u00fc\u0001\u0000\u0000\u0000"+
		"\u0104\u0105\u0001\u0000\u0000\u0000\u0105\u0106\u0001\u0000\u0000\u0000"+
		"\u0106\u0107\u0005\u0011\u0000\u0000\u0107\u0108\u0005\u0014\u0000\u0000"+
		"\u0108\u0109\u0003&\u0013\u0000\u0109\u0119\u0001\u0000\u0000\u0000\u010a"+
		"\u0113\u0003\u0004\u0002\u0000\u010b\u0110\u0003\u0002\u0001\u0000\u010c"+
		"\u010d\u0005\u0012\u0000\u0000\u010d\u010f\u0003\u0002\u0001\u0000\u010e"+
		"\u010c\u0001\u0000\u0000\u0000\u010f\u0112\u0001\u0000\u0000\u0000\u0110"+
		"\u010e\u0001\u0000\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000\u0111"+
		"\u0114\u0001\u0000\u0000\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0113"+
		"\u010b\u0001\u0000\u0000\u0000\u0113\u0114\u0001\u0000\u0000\u0000\u0114"+
		"\u0115\u0001\u0000\u0000\u0000\u0115\u0116\u0005\u0014\u0000\u0000\u0116"+
		"\u0117\u0003&\u0013\u0000\u0117\u0119\u0001\u0000\u0000\u0000\u0118\u00f5"+
		"\u0001\u0000\u0000\u0000\u0118\u00f6\u0001\u0000\u0000\u0000\u0118\u00fa"+
		"\u0001\u0000\u0000\u0000\u0118\u010a\u0001\u0000\u0000\u0000\u0119\u001b"+
		"\u0001\u0000\u0000\u0000\u011a\u011b\u0003\u0006\u0003\u0000\u011b\u011c"+
		"\u0003\u0004\u0002\u0000\u011c\u0121\u0003\f\u0006\u0000\u011d\u011e\u0005"+
		"\u0010\u0000\u0000\u011e\u011f\u0003\u001e\u000f\u0000\u011f\u0120\u0005"+
		"\u0011\u0000\u0000\u0120\u0122\u0001\u0000\u0000\u0000\u0121\u011d\u0001"+
		"\u0000\u0000\u0000\u0121\u0122\u0001\u0000\u0000\u0000\u0122\u0123\u0001"+
		"\u0000\u0000\u0000\u0123\u0124\u0005\u0013\u0000\u0000\u0124\u0125\u0003"+
		"\u0016\u000b\u0000\u0125\u012e\u0001\u0000\u0000\u0000\u0126\u0127\u0003"+
		"\u0006\u0003\u0000\u0127\u0128\u0003\u0004\u0002\u0000\u0128\u0129\u0003"+
		"\f\u0006\u0000\u0129\u012a\u0003\u001e\u000f\u0000\u012a\u012b\u0005\u0013"+
		"\u0000\u0000\u012b\u012c\u0003\u0016\u000b\u0000\u012c\u012e\u0001\u0000"+
		"\u0000\u0000\u012d\u011a\u0001\u0000\u0000\u0000\u012d\u0126\u0001\u0000"+
		"\u0000\u0000\u012e\u001d\u0001\u0000\u0000\u0000\u012f\u0130\u0003\u0002"+
		"\u0001\u0000\u0130\u0131\u0005\u0013\u0000\u0000\u0131\u0139\u0003\u0016"+
		"\u000b\u0000\u0132\u0133\u0005\u0012\u0000\u0000\u0133\u0134\u0003\u0002"+
		"\u0001\u0000\u0134\u0135\u0005\u0013\u0000\u0000\u0135\u0136\u0003\u0016"+
		"\u000b\u0000\u0136\u0138\u0001\u0000\u0000\u0000\u0137\u0132\u0001\u0000"+
		"\u0000\u0000\u0138\u013b\u0001\u0000\u0000\u0000\u0139\u0137\u0001\u0000"+
		"\u0000\u0000\u0139\u013a\u0001\u0000\u0000\u0000\u013a\u013d\u0001\u0000"+
		"\u0000\u0000\u013b\u0139\u0001\u0000\u0000\u0000\u013c\u012f\u0001\u0000"+
		"\u0000\u0000\u013c\u013d\u0001\u0000\u0000\u0000\u013d\u001f\u0001\u0000"+
		"\u0000\u0000\u013e\u013f\u0005\t\u0000\u0000\u013f\u0140\u0003\u0000\u0000"+
		"\u0000\u0140\u0141\u0003\u000e\u0007\u0000\u0141\u0142\u0005\n\u0000\u0000"+
		"\u0142\u0143\u0003\u0000\u0000\u0000\u0143\u0144\u0005\u0012\u0000\u0000"+
		"\u0144!\u0001\u0000\u0000\u0000\u0145\u0146\u0005\u001a\u0000\u0000\u0146"+
		"#\u0001\u0000\u0000\u0000\u0147\u014c\u0003\u0002\u0001\u0000\u0148\u014c"+
		"\u0003\b\u0004\u0000\u0149\u014c\u0003\u0012\t\u0000\u014a\u014c\u0003"+
		"\"\u0011\u0000\u014b\u0147\u0001\u0000\u0000\u0000\u014b\u0148\u0001\u0000"+
		"\u0000\u0000\u014b\u0149\u0001\u0000\u0000\u0000\u014b\u014a\u0001\u0000"+
		"\u0000\u0000\u014c%\u0001\u0000\u0000\u0000\u014d\u0151\u0003$\u0012\u0000"+
		"\u014e\u0150\u0003(\u0014\u0000\u014f\u014e\u0001\u0000\u0000\u0000\u0150"+
		"\u0153\u0001\u0000\u0000\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0151"+
		"\u0152\u0001\u0000\u0000\u0000\u0152\'\u0001\u0000\u0000\u0000\u0153\u0151"+
		"\u0001\u0000\u0000\u0000\u0154\u0155\u0003\u0004\u0002\u0000\u0155\u0156"+
		"\u0003\u000e\u0007\u0000\u0156\u0175\u0001\u0000\u0000\u0000\u0157\u0158"+
		"\u0003\u0004\u0002\u0000\u0158\u0159\u0003\u000e\u0007\u0000\u0159\u0161"+
		"\u0005\u0010\u0000\u0000\u015a\u015d\u0003&\u0013\u0000\u015b\u015c\u0005"+
		"\u0012\u0000\u0000\u015c\u015e\u0003&\u0013\u0000\u015d\u015b\u0001\u0000"+
		"\u0000\u0000\u015e\u015f\u0001\u0000\u0000\u0000\u015f\u015d\u0001\u0000"+
		"\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000\u0160\u0162\u0001\u0000"+
		"\u0000\u0000\u0161\u015a\u0001\u0000\u0000\u0000\u0161\u0162\u0001\u0000"+
		"\u0000\u0000\u0162\u0163\u0001\u0000\u0000\u0000\u0163\u0164\u0005\u0011"+
		"\u0000\u0000\u0164\u0175\u0001\u0000\u0000\u0000\u0165\u0166\u0003\u0004"+
		"\u0002\u0000\u0166\u0167\u0003\u000e\u0007\u0000\u0167\u0168\u0003$\u0012"+
		"\u0000\u0168\u0175\u0001\u0000\u0000\u0000\u0169\u016a\u0003\u0004\u0002"+
		"\u0000\u016a\u016b\u0003\u000e\u0007\u0000\u016b\u016c\u0003\u0002\u0001"+
		"\u0000\u016c\u016d\u0005\b\u0000\u0000\u016d\u0171\u0003$\u0012\u0000"+
		"\u016e\u0170\u0003(\u0014\u0000\u016f\u016e\u0001\u0000\u0000\u0000\u0170"+
		"\u0173\u0001\u0000\u0000\u0000\u0171\u016f\u0001\u0000\u0000\u0000\u0171"+
		"\u0172\u0001\u0000\u0000\u0000\u0172\u0175\u0001\u0000\u0000\u0000\u0173"+
		"\u0171\u0001\u0000\u0000\u0000\u0174\u0154\u0001\u0000\u0000\u0000\u0174"+
		"\u0157\u0001\u0000\u0000\u0000\u0174\u0165\u0001\u0000\u0000\u0000\u0174"+
		"\u0169\u0001\u0000\u0000\u0000\u0175)\u0001\u0000\u0000\u0000\u0176\u0177"+
		"\u0003&\u0013\u0000\u0177\u0178\u0005\u0000\u0000\u0001\u0178+\u0001\u0000"+
		"\u0000\u0000\u0179\u017a\u0003\u0002\u0001\u0000\u017a\u017b\u0005\u0000"+
		"\u0000\u0001\u017b-\u0001\u0000\u0000\u0000\u017c\u017d\u0003\u0004\u0002"+
		"\u0000\u017d\u017e\u0005\u0000\u0000\u0001\u017e/\u0001\u0000\u0000\u0000"+
		"\u017f\u0180\u0003\u0000\u0000\u0000\u0180\u0181\u0005\u0000\u0000\u0001"+
		"\u01811\u0001\u0000\u0000\u0000\u0182\u0183\u0003\u0016\u000b\u0000\u0183"+
		"\u0184\u0005\u0000\u0000\u0001\u01843\u0001\u0000\u0000\u0000\u0185\u0189"+
		"\u0005 \u0000\u0000\u0186\u0188\u0003 \u0010\u0000\u0187\u0186\u0001\u0000"+
		"\u0000\u0000\u0188\u018b\u0001\u0000\u0000\u0000\u0189\u0187\u0001\u0000"+
		"\u0000\u0000\u0189\u018a\u0001\u0000\u0000\u0000\u018a\u018f\u0001\u0000"+
		"\u0000\u0000\u018b\u0189\u0001\u0000\u0000\u0000\u018c\u018e\u0003\u0010"+
		"\b\u0000\u018d\u018c\u0001\u0000\u0000\u0000\u018e\u0191\u0001\u0000\u0000"+
		"\u0000\u018f\u018d\u0001\u0000\u0000\u0000\u018f\u0190\u0001\u0000\u0000"+
		"\u0000\u0190\u0192\u0001\u0000\u0000\u0000\u0191\u018f\u0001\u0000\u0000"+
		"\u0000\u0192\u0193\u0005\u0000\u0000\u0001\u01935\u0001\u0000\u0000\u0000"+
		".DQXZcfirux\u0082\u0086\u0088\u0093\u009a\u009e\u00a2\u00a9\u00ac\u00af"+
		"\u00b6\u00c1\u00c3\u00c9\u00dc\u00e0\u00eb\u00ee\u00f3\u0101\u0104\u0110"+
		"\u0113\u0118\u0121\u012d\u0139\u013c\u014b\u0151\u015f\u0161\u0171\u0174"+
		"\u0189\u018f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}