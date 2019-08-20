/*
 * Copyright (C) 2018 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
// Generated from ../src/main/antlr/CQLCF.g4 by ANTLR 4.5.1
package de.qaware.chronix.cql.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CQLCFParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, LOWERCASE_STRING = 6, STRING_AND_NUMBERS_UPPERCASE = 7;
    public static final int
            RULE_cqlcf = 0, RULE_chronixTypedFunctions = 1, RULE_chronixTypedFunction = 2,
            RULE_chronixType = 3, RULE_chronixfunction = 4, RULE_name = 5, RULE_parameter = 6;
    public static final String[] ruleNames = {
            "cqlcf", "chronixTypedFunctions", "chronixTypedFunction", "chronixType",
            "chronixfunction", "name", "parameter"
    };
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\t<\4\2\t\2\4\3\t" +
                    "\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\3\3\3\5\3\25\n\3" +
                    "\3\3\7\3\30\n\3\f\3\16\3\33\13\3\3\4\3\4\3\4\3\4\3\4\7\4\"\n\4\f\4\16" +
                    "\4%\13\4\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\7\6\61\n\6\f\6\16\6\64" +
                    "\13\6\5\6\66\n\6\3\7\3\7\3\b\3\b\3\b\2\2\t\2\4\6\b\n\f\16\2\29\2\20\3" +
                    "\2\2\2\4\22\3\2\2\2\6\34\3\2\2\2\b(\3\2\2\2\n\65\3\2\2\2\f\67\3\2\2\2" +
                    "\169\3\2\2\2\20\21\5\4\3\2\21\3\3\2\2\2\22\31\5\6\4\2\23\25\7\3\2\2\24" +
                    "\23\3\2\2\2\24\25\3\2\2\2\25\26\3\2\2\2\26\30\5\6\4\2\27\24\3\2\2\2\30" +
                    "\33\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\5\3\2\2\2\33\31\3\2\2\2\34" +
                    "\35\5\b\5\2\35\36\7\4\2\2\36#\5\n\6\2\37 \7\3\2\2 \"\5\n\6\2!\37\3\2\2" +
                    "\2\"%\3\2\2\2#!\3\2\2\2#$\3\2\2\2$&\3\2\2\2%#\3\2\2\2&\'\7\5\2\2\'\7\3" +
                    "\2\2\2()\7\b\2\2)\t\3\2\2\2*\66\5\f\7\2+,\5\f\7\2,-\7\6\2\2-\62\5\16\b" +
                    "\2./\7\7\2\2/\61\5\16\b\2\60.\3\2\2\2\61\64\3\2\2\2\62\60\3\2\2\2\62\63" +
                    "\3\2\2\2\63\66\3\2\2\2\64\62\3\2\2\2\65*\3\2\2\2\65+\3\2\2\2\66\13\3\2" +
                    "\2\2\678\7\b\2\28\r\3\2\2\29:\7\t\2\2:\17\3\2\2\2\7\24\31#\62\65";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = {
            null, "';'", "'{'", "'}'", "':'", "','"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, null, null, null, null, null, "LOWERCASE_STRING", "STRING_AND_NUMBERS_UPPERCASE"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    static {
        RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION);
    }

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

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    public CQLCFParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
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
    public String getGrammarFileName() {
        return "CQLCF.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public final CqlcfContext cqlcf() throws RecognitionException {
        CqlcfContext _localctx = new CqlcfContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_cqlcf);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(14);
                chronixTypedFunctions();
            }
        } catch (RecognitionException re) {
            logger().info("re Exception: ", re.getMessage());
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("cqlcf Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixTypedFunctionsContext chronixTypedFunctions() throws RecognitionException {
        ChronixTypedFunctionsContext _localctx = new ChronixTypedFunctionsContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_chronixTypedFunctions);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(16);
                chronixTypedFunction();
                setState(23);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__0 || _la == LOWERCASE_STRING) {
                    {
                        {
                            setState(18);
                            _la = _input.LA(1);
                            if (_la == T__0) {
                                {
                                    setState(17);
                                    match(T__0);
                                }
                            }

                            setState(20);
                            chronixTypedFunction();
                        }
                    }
                    setState(25);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            logger().info("re Exception: ", re.getMessage());
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("cfs Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixTypedFunctionContext chronixTypedFunction() throws RecognitionException {
        ChronixTypedFunctionContext _localctx = new ChronixTypedFunctionContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_chronixTypedFunction);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(26);
                chronixType();
                setState(27);
                match(T__1);
                setState(28);
                chronixfunction();
                setState(33);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__0) {
                    {
                        {
                            setState(29);
                            match(T__0);
                            setState(30);
                            chronixfunction();
                        }
                    }
                    setState(35);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(36);
                match(T__2);
            }
        } catch (RecognitionException re) {
            logger().info("re Exception: ", re.getMessage());
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("ctf Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    private Logger logger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public final ChronixTypeContext chronixType() throws RecognitionException {
        ChronixTypeContext _localctx = new ChronixTypeContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_chronixType);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(38);
                match(LOWERCASE_STRING);
            }
        } catch (RecognitionException re) {
            logger().info("re Exception: ", re.getMessage());
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("ct Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixfunctionContext chronixfunction() throws RecognitionException {
        ChronixfunctionContext _localctx = new ChronixfunctionContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_chronixfunction);
        int _la;
        try {
            setState(51);
            switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(40);
                    name();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(41);
                    name();
                    setState(42);
                    match(T__3);
                    setState(43);
                    parameter();
                    setState(48);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == T__4) {
                        {
                            {
                                setState(44);
                                match(T__4);
                                setState(45);
                                parameter();
                            }
                        }
                        setState(50);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                break;
            }
        } catch (RecognitionException re) {
            logger().info("re Exception: ", re.getMessage());
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("cf Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final NameContext name() throws RecognitionException {
        NameContext _localctx = new NameContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(53);
                match(LOWERCASE_STRING);
            }
        } catch (RecognitionException re) {
            logger().info("re Exception: ", re.getMessage());
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("name Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ParameterContext parameter() throws RecognitionException {
        ParameterContext _localctx = new ParameterContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_parameter);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(55);
                match(STRING_AND_NUMBERS_UPPERCASE);
            }
        } catch (RecognitionException re) {
            re.printStackTrace();
            logger().info("re Exception: ");
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } catch (Exception e) {
            logger().info("parameter Exception: ", e);
            throw e;
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class CqlcfContext extends ParserRuleContext {
        public CqlcfContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ChronixTypedFunctionsContext chronixTypedFunctions() {
            return getRuleContext(ChronixTypedFunctionsContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_cqlcf;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterCqlcf(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitCqlcf(this);
        }
    }

    public static class ChronixTypedFunctionsContext extends ParserRuleContext {
        public ChronixTypedFunctionsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<ChronixTypedFunctionContext> chronixTypedFunction() {
            return getRuleContexts(ChronixTypedFunctionContext.class);
        }

        public ChronixTypedFunctionContext chronixTypedFunction(int i) {
            return getRuleContext(ChronixTypedFunctionContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixTypedFunctions;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterChronixTypedFunctions(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitChronixTypedFunctions(this);
        }
    }

    public static class ChronixTypedFunctionContext extends ParserRuleContext {
        public ChronixTypedFunctionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ChronixTypeContext chronixType() {
            return getRuleContext(ChronixTypeContext.class, 0);
        }

        public List<ChronixfunctionContext> chronixfunction() {
            return getRuleContexts(ChronixfunctionContext.class);
        }

        public ChronixfunctionContext chronixfunction(int i) {
            return getRuleContext(ChronixfunctionContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixTypedFunction;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterChronixTypedFunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitChronixTypedFunction(this);
        }
    }

    public static class ChronixTypeContext extends ParserRuleContext {
        public ChronixTypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode LOWERCASE_STRING() {
            return getToken(CQLCFParser.LOWERCASE_STRING, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixType;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterChronixType(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitChronixType(this);
        }
    }

    public static class ChronixfunctionContext extends ParserRuleContext {
        public ChronixfunctionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        public List<ParameterContext> parameter() {
            return getRuleContexts(ParameterContext.class);
        }

        public ParameterContext parameter(int i) {
            return getRuleContext(ParameterContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixfunction;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterChronixfunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitChronixfunction(this);
        }
    }

    public static class NameContext extends ParserRuleContext {
        public NameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode LOWERCASE_STRING() {
            return getToken(CQLCFParser.LOWERCASE_STRING, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitName(this);
        }
    }

    public static class ParameterContext extends ParserRuleContext {
        public ParameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode STRING_AND_NUMBERS_UPPERCASE() {
            return getToken(CQLCFParser.STRING_AND_NUMBERS_UPPERCASE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_parameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).enterParameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLCFListener) ((CQLCFListener) listener).exitParameter(this);
        }
    }
}