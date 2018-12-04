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
// Generated from ../src/main/antlr/CQL.g4 by ANTLR 4.7.1
package de.qaware.chronix.cql.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CQLParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, NEWLINE = 8, STRING = 9,
            INT = 10;
    public static final int
            RULE_cql = 0, RULE_chronixJoinParameter = 1, RULE_chronixJoinFields = 2,
            RULE_chronixJoinField = 3, RULE_chronixFunctionParameter = 4, RULE_chronixTypedFunctions = 5,
            RULE_chronixTypedFunction = 6, RULE_chronixfunction = 7, RULE_name = 8,
            RULE_parameter = 9, RULE_chronixType = 10;
    public static final String[] ruleNames = {
            "cql", "chronixJoinParameter", "chronixJoinFields", "chronixJoinField",
            "chronixFunctionParameter", "chronixTypedFunctions", "chronixTypedFunction",
            "chronixfunction", "name", "parameter", "chronixType"
    };
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\f`\4\2\t\2\4\3\t" +
                    "\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4" +
                    "\f\t\f\3\2\5\2\32\n\2\3\2\5\2\35\n\2\3\3\3\3\3\3\3\4\3\4\5\4$\n\4\3\4" +
                    "\7\4\'\n\4\f\4\16\4*\13\4\3\5\3\5\3\6\3\6\3\6\3\7\3\7\5\7\63\n\7\3\7\7" +
                    "\7\66\n\7\f\7\16\79\13\7\3\b\3\b\3\b\3\b\5\b?\n\b\3\b\7\bB\n\b\f\b\16" +
                    "\bE\13\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\7\tO\n\t\f\t\16\tR\13\t\5\tT" +
                    "\n\t\3\n\3\n\3\13\3\13\3\13\3\13\5\13\\\n\13\3\f\3\f\3\f\2\2\r\2\4\6\b" +
                    "\n\f\16\20\22\24\26\2\2\2`\2\31\3\2\2\2\4\36\3\2\2\2\6!\3\2\2\2\b+\3\2" +
                    "\2\2\n-\3\2\2\2\f\60\3\2\2\2\16:\3\2\2\2\20S\3\2\2\2\22U\3\2\2\2\24[\3" +
                    "\2\2\2\26]\3\2\2\2\30\32\5\4\3\2\31\30\3\2\2\2\31\32\3\2\2\2\32\34\3\2" +
                    "\2\2\33\35\5\n\6\2\34\33\3\2\2\2\34\35\3\2\2\2\35\3\3\2\2\2\36\37\7\3" +
                    "\2\2\37 \5\6\4\2 \5\3\2\2\2!(\5\b\5\2\"$\7\4\2\2#\"\3\2\2\2#$\3\2\2\2" +
                    "$%\3\2\2\2%\'\5\b\5\2&#\3\2\2\2\'*\3\2\2\2(&\3\2\2\2()\3\2\2\2)\7\3\2" +
                    "\2\2*(\3\2\2\2+,\7\13\2\2,\t\3\2\2\2-.\7\5\2\2./\5\f\7\2/\13\3\2\2\2\60" +
                    "\67\5\16\b\2\61\63\7\6\2\2\62\61\3\2\2\2\62\63\3\2\2\2\63\64\3\2\2\2\64" +
                    "\66\5\16\b\2\65\62\3\2\2\2\669\3\2\2\2\67\65\3\2\2\2\678\3\2\2\28\r\3" +
                    "\2\2\29\67\3\2\2\2:;\5\26\f\2;<\7\7\2\2<C\5\20\t\2=?\7\6\2\2>=\3\2\2\2" +
                    ">?\3\2\2\2?@\3\2\2\2@B\5\20\t\2A>\3\2\2\2BE\3\2\2\2CA\3\2\2\2CD\3\2\2" +
                    "\2DF\3\2\2\2EC\3\2\2\2FG\7\b\2\2G\17\3\2\2\2HT\5\22\n\2IJ\5\22\n\2JK\7" +
                    "\t\2\2KP\5\24\13\2LM\7\4\2\2MO\5\24\13\2NL\3\2\2\2OR\3\2\2\2PN\3\2\2\2" +
                    "PQ\3\2\2\2QT\3\2\2\2RP\3\2\2\2SH\3\2\2\2SI\3\2\2\2T\21\3\2\2\2UV\7\13" +
                    "\2\2V\23\3\2\2\2W\\\7\13\2\2XY\7\f\2\2Y\\\7\13\2\2Z\\\7\f\2\2[W\3\2\2" +
                    "\2[X\3\2\2\2[Z\3\2\2\2\\\25\3\2\2\2]^\7\13\2\2^\27\3\2\2\2\r\31\34#(\62" +
                    "\67>CPS[";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = {
            null, "'&cj='", "','", "'&cf='", "';'", "'{'", "'}'", "':'"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, null, null, null, null, null, null, null, "NEWLINE", "STRING", "INT"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    static {
        RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION);
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

    public CQLParser(TokenStream input) {
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
        return "CQL.g4";
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

    public final CqlContext cql() throws RecognitionException {
        CqlContext _localctx = new CqlContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_cql);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(23);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__0) {
                    {
                        setState(22);
                        chronixJoinParameter();
                    }
                }

                setState(26);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__2) {
                    {
                        setState(25);
                        chronixFunctionParameter();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixJoinParameterContext chronixJoinParameter() throws RecognitionException {
        ChronixJoinParameterContext _localctx = new ChronixJoinParameterContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_chronixJoinParameter);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(28);
                match(T__0);
                setState(29);
                chronixJoinFields();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixJoinFieldsContext chronixJoinFields() throws RecognitionException {
        ChronixJoinFieldsContext _localctx = new ChronixJoinFieldsContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_chronixJoinFields);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(31);
                chronixJoinField();
                setState(38);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__1 || _la == STRING) {
                    {
                        {
                            setState(33);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == T__1) {
                                {
                                    setState(32);
                                    match(T__1);
                                }
                            }

                            setState(35);
                            chronixJoinField();
                        }
                    }
                    setState(40);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixJoinFieldContext chronixJoinField() throws RecognitionException {
        ChronixJoinFieldContext _localctx = new ChronixJoinFieldContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_chronixJoinField);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(41);
                match(STRING);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixFunctionParameterContext chronixFunctionParameter() throws RecognitionException {
        ChronixFunctionParameterContext _localctx = new ChronixFunctionParameterContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_chronixFunctionParameter);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(43);
                match(T__2);
                setState(44);
                chronixTypedFunctions();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixTypedFunctionsContext chronixTypedFunctions() throws RecognitionException {
        ChronixTypedFunctionsContext _localctx = new ChronixTypedFunctionsContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_chronixTypedFunctions);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(46);
                chronixTypedFunction();
                setState(53);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__3 || _la == STRING) {
                    {
                        {
                            setState(48);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == T__3) {
                                {
                                    setState(47);
                                    match(T__3);
                                }
                            }

                            setState(50);
                            chronixTypedFunction();
                        }
                    }
                    setState(55);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixTypedFunctionContext chronixTypedFunction() throws RecognitionException {
        ChronixTypedFunctionContext _localctx = new ChronixTypedFunctionContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_chronixTypedFunction);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(56);
                chronixType();
                setState(57);
                match(T__4);
                setState(58);
                chronixfunction();
                setState(65);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__3 || _la == STRING) {
                    {
                        {
                            setState(60);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == T__3) {
                                {
                                    setState(59);
                                    match(T__3);
                                }
                            }

                            setState(62);
                            chronixfunction();
                        }
                    }
                    setState(67);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(68);
                match(T__5);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixfunctionContext chronixfunction() throws RecognitionException {
        ChronixfunctionContext _localctx = new ChronixfunctionContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_chronixfunction);
        int _la;
        try {
            setState(81);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 9, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(70);
                    name();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(71);
                    name();
                    setState(72);
                    match(T__6);
                    setState(73);
                    parameter();
                    setState(78);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == T__1) {
                        {
                            {
                                setState(74);
                                match(T__1);
                                setState(75);
                                parameter();
                            }
                        }
                        setState(80);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final NameContext name() throws RecognitionException {
        NameContext _localctx = new NameContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(83);
                match(STRING);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ParameterContext parameter() throws RecognitionException {
        ParameterContext _localctx = new ParameterContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_parameter);
        try {
            setState(89);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 10, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(85);
                    match(STRING);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(86);
                    match(INT);
                    setState(87);
                    match(STRING);
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(88);
                    match(INT);
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ChronixTypeContext chronixType() throws RecognitionException {
        ChronixTypeContext _localctx = new ChronixTypeContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_chronixType);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(91);
                match(STRING);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class CqlContext extends ParserRuleContext {
        public CqlContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ChronixJoinParameterContext chronixJoinParameter() {
            return getRuleContext(ChronixJoinParameterContext.class, 0);
        }

        public ChronixFunctionParameterContext chronixFunctionParameter() {
            return getRuleContext(ChronixFunctionParameterContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_cql;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterCql(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitCql(this);
        }
    }

    public static class ChronixJoinParameterContext extends ParserRuleContext {
        public ChronixJoinParameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ChronixJoinFieldsContext chronixJoinFields() {
            return getRuleContext(ChronixJoinFieldsContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixJoinParameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixJoinParameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixJoinParameter(this);
        }
    }

    public static class ChronixJoinFieldsContext extends ParserRuleContext {
        public ChronixJoinFieldsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<ChronixJoinFieldContext> chronixJoinField() {
            return getRuleContexts(ChronixJoinFieldContext.class);
        }

        public ChronixJoinFieldContext chronixJoinField(int i) {
            return getRuleContext(ChronixJoinFieldContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixJoinFields;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixJoinFields(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixJoinFields(this);
        }
    }

    public static class ChronixJoinFieldContext extends ParserRuleContext {
        public ChronixJoinFieldContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode STRING() {
            return getToken(CQLParser.STRING, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixJoinField;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixJoinField(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixJoinField(this);
        }
    }

    public static class ChronixFunctionParameterContext extends ParserRuleContext {
        public ChronixFunctionParameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ChronixTypedFunctionsContext chronixTypedFunctions() {
            return getRuleContext(ChronixTypedFunctionsContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixFunctionParameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixFunctionParameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixFunctionParameter(this);
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
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixTypedFunctions(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixTypedFunctions(this);
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
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixTypedFunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixTypedFunction(this);
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
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixfunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixfunction(this);
        }
    }

    public static class NameContext extends ParserRuleContext {
        public NameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode STRING() {
            return getToken(CQLParser.STRING, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitName(this);
        }
    }

    public static class ParameterContext extends ParserRuleContext {
        public ParameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode STRING() {
            return getToken(CQLParser.STRING, 0);
        }

        public TerminalNode INT() {
            return getToken(CQLParser.INT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_parameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterParameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitParameter(this);
        }
    }

    public static class ChronixTypeContext extends ParserRuleContext {
        public ChronixTypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode STRING() {
            return getToken(CQLParser.STRING, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_chronixType;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).enterChronixType(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof CQLListener) ((CQLListener) listener).exitChronixType(this);
        }
    }
}