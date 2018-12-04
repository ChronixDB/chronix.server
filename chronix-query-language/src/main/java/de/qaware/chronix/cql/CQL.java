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


import de.qaware.chronix.cql.antlr.CQLLexer;
import de.qaware.chronix.cql.antlr.CQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(CQL.class);

    //First parsing is faster.
    private final CQLLexer lexer = new CQLLexer(null);
    private final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    private final CQLParser parser = new CQLParser(tokenStream);

    public CQLParseResult parse(String cql) throws CQLException {
        long start = System.nanoTime();

        init(cql);
        CQLParseResult parseResult = parseChronixFunctionParameter(this.parser.cql());

        long stop = System.nanoTime();
        LOGGER.error("Took {} ms", (stop - start) / 1e6);

        return parseResult;
    }

    private void init(String cql) {
        CodePointCharStream input = CharStreams.fromString(cql);
        this.lexer.setInputStream(input);
        this.tokenStream.setTokenSource(lexer);
        this.parser.setTokenStream(tokenStream);
    }

    private CQLParseResult parseChronixFunctionParameter(CQLParser.CqlContext tree) throws CQLException {
        CQLParseResult parseResult = new CQLParseResult();

        checkAndThrow(tree);

        CQLParser.ChronixFunctionParameterContext functionParameter = tree.chronixFunctionParameter();
        checkAndThrow(functionParameter);

        CQLParser.ChronixTypedFunctionsContext chronixTypedFunctions = functionParameter.chronixTypedFunctions();
        checkAndThrow(chronixTypedFunctions);

        List<CQLParser.ChronixTypedFunctionContext> chronixTypedFunction = chronixTypedFunctions.chronixTypedFunction();


        for (CQLParser.ChronixTypedFunctionContext typedFunction : chronixTypedFunction) {
            checkAndThrow(typedFunction);

            String type = typedFunction.chronixType().getText();

            List<CQLParser.ChronixfunctionContext> chronixFunctions = typedFunction.chronixfunction();
            for (CQLParser.ChronixfunctionContext chronixFunction : chronixFunctions) {
                checkAndThrow(chronixFunction);

                String name = chronixFunction.name().getText();
                List<String> parameters = convertToStringList(chronixFunction.parameter());

                parseResult.addFunctionForType(type, new ChronixFunction(name, parameters));
            }

        }

        return parseResult;
    }

    private void checkAndThrow(ParserRuleContext ruleContext) throws CQLException {
        if (ruleContext.exception != null) {
            //TODO: Extract useful information and throw
            throw new CQLException(ruleContext.exception.getMessage(), ruleContext.exception);
        }
    }

    private List<String> convertToStringList(List<CQLParser.ParameterContext> parameter) {

        List<String> parameters = new ArrayList<>();
        for (CQLParser.ParameterContext parameterContext : parameter) {
            parameters.add(parameterContext.getText());
        }

        return parameters;
    }

    class CQLParseResult {


        private Set<String> joinFields = new HashSet<>();
        private Map<String, List<ChronixFunction>> chronixFunctions = new HashMap<>();

        public void addJoinField(String text) {
            this.joinFields.add(text);
        }

        public Set<String> getJoinFields() {
            return joinFields;
        }

        public void addFunctionForType(String type, ChronixFunction chronixFunction) {
            if (!chronixFunctions.containsKey(type)) {
                chronixFunctions.put(type, new ArrayList<>());
            }

            this.chronixFunctions.get(type).add(chronixFunction);
        }

        @Override
        public String toString() {
            return "CQLParseResult{" + "joinFields=" + joinFields +
                    ", chronixFunctions=" + chronixFunctions +
                    '}';
        }
    }

}
