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
package de.qaware.chronix.cql;


import de.qaware.chronix.cql.antlr.CQLCFLexer;
import de.qaware.chronix.cql.antlr.CQLCFParser;
import de.qaware.chronix.server.functions.ChronixAggregation;
import de.qaware.chronix.server.functions.ChronixAnalysis;
import de.qaware.chronix.server.functions.ChronixFunction;
import de.qaware.chronix.server.functions.ChronixTransformation;
import de.qaware.chronix.server.types.ChronixType;
import de.qaware.chronix.server.types.ChronixTypes;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;
import java.util.List;

public class CQL {

    //First parsing is faster.
    private final CQLCFLexer lexer = new CQLCFLexer(null);
    private final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    private final CQLCFParser parser = new CQLCFParser(tokenStream);
    private final ChronixTypes knownChronixTypes;
    private final de.qaware.chronix.server.functions.plugin.ChronixFunctions knownChronixFunctions;
    private final CQLErrorListener errorListener;


    public CQL(ChronixTypes plugInTypes, de.qaware.chronix.server.functions.plugin.ChronixFunctions plugInFunctions) {
        this.knownChronixTypes = plugInTypes;
        this.knownChronixFunctions = plugInFunctions;
        this.errorListener = new CQLErrorListener();
        parser.removeErrorListeners();
        lexer.removeErrorListeners();

        this.parser.addErrorListener(errorListener);
        this.lexer.addErrorListener(errorListener);
    }

    /**
     * Converts the given Chronix Join Parameter into a CQLJoinFunction
     *
     * @param cj the chronix join parameter
     * @return a join function
     * @throws CQLException (not thrown at the moment)
     */
    public CQLJoinFunction parseCJ(String cj) throws CQLException {
        return new CQLJoinFunction(cj);
    }

    /**
     * Parses the given Chronix Function parameter
     *
     * @param cf the Chronix Function parameter
     * @return the parse result containing the Chronix Functions
     * @throws CQLException if the given query string (value of cf) is invalid
     */
    public CQLCFResult parseCF(String cf) throws CQLException {
        if (cf == null || cf.isEmpty()) {
            return new CQLCFResult();
        }
        init(cf);

        return parseChronixFunctionParameter(this.parser.cqlcf());
    }

    private void init(String cql) {
        this.errorListener.setQuery(cql);

        // Antlr 4.7.1
        // CodePointCharStream input = CharStreams.fromString(cql);

        CharStream charStream = new ANTLRInputStream(cql);

        this.lexer.setInputStream(charStream);
        // Antlr 4.7.1
        //this.tokenStream.setTokenSource(lexer);

        UnbufferedTokenStream tokenStream = new UnbufferedTokenStream(lexer);

        this.parser.setTokenStream(tokenStream);

    }

    private CQLCFResult parseChronixFunctionParameter(CQLCFParser.CqlcfContext tree) throws CQLException {

        CQLCFParser.ChronixTypedFunctionsContext chronixTypedFunctions = tree.chronixTypedFunctions();
        List<CQLCFParser.ChronixTypedFunctionContext> chronixTypedFunction = chronixTypedFunctions.chronixTypedFunction();

        CQLCFResult parseResult = new CQLCFResult();

        for (CQLCFParser.ChronixTypedFunctionContext typedFunction : chronixTypedFunction) {

            String type = typedFunction.chronixType().getText();
            //Check if Chronix knows the type
            ChronixType chronixType = knownChronixTypes.getTypeForName(type);
            if (chronixType == null) {
                throw new CQLException("Type '" + type + "' in query '" + this.errorListener.cql + "' is unknown.");
            }

            ChronixFunctions resultingTypeFunctions = new ChronixFunctions();

            //Check if Chronix knows the functions
            List<CQLCFParser.ChronixfunctionContext> chronixFunctions = typedFunction.chronixfunction();
            for (CQLCFParser.ChronixfunctionContext chronixFunction : chronixFunctions) {

                String name = chronixFunction.name().getText();
                ChronixFunction function = chronixType.getFunction(name);
                if (function == null) {
                    //check the plugin functions
                    function = knownChronixFunctions.getFunctionForQueryName(type, name);
                    if (function == null) {
                        throw new CQLException("Function '" + name + "' in query '" + this.errorListener.cql + "' is unknown.");
                    }
                }

                //if we get here, we have valid type and a valid function
                //Set the arguments to the function
                function.setArguments(asStringArray(chronixFunction.parameter()));

                switch (function.getFunctionType()) {
                    case AGGREGATION:
                        resultingTypeFunctions.addAggregation((ChronixAggregation) function);
                        break;
                    case TRANSFORMATION:
                        resultingTypeFunctions.addTransformation((ChronixTransformation) function);
                        break;
                    case ANALYSIS:
                        resultingTypeFunctions.addAnalysis((ChronixAnalysis) function);
                        break;
                    default:
                        //ignore
                        break;

                }

            }
            parseResult.addChronixFunctionsForType(chronixType, resultingTypeFunctions);
        }
        return parseResult;
    }


    private String[] asStringArray(List<CQLCFParser.ParameterContext> parameter) {

        String[] functionArguments = new String[parameter.size()];
        for (int i = 0; i < parameter.size(); i++) {
            functionArguments[i] = parameter.get(i).getText();
        }
        return functionArguments;
    }


    private final class CQLErrorListener implements ANTLRErrorListener {
        private String cql;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {

            if (offendingSymbol instanceof CommonToken) {
                CommonToken offendingToken = (CommonToken) offendingSymbol;
                StringBuilder annotatedCQL = new StringBuilder(cql);
                annotatedCQL.insert(offendingToken.getStopIndex() + 1, "'<-this-'");
                cql = annotatedCQL.toString();
            }


            throw new CQLException("Syntax error: " + msg + " at line: '" + line + "', position: '" + charPositionInLine + "', query '" + cql + "'");
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
            throw new CQLException("reportAmbiguity");
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
            throw new CQLException("reportAttemptingFullContext");
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
            throw new CQLException("reportContextSensitivity");
        }

        public void setQuery(String cql) {
            this.cql = cql;
        }
    }
}
