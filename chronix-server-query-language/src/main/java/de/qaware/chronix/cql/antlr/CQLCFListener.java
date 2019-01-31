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

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CQLCFParser}.
 */
public interface CQLCFListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link CQLCFParser#cqlcf}.
     *
     * @param ctx the parse tree
     */
    void enterCqlcf(CQLCFParser.CqlcfContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#cqlcf}.
     *
     * @param ctx the parse tree
     */
    void exitCqlcf(CQLCFParser.CqlcfContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLCFParser#chronixTypedFunctions}.
     *
     * @param ctx the parse tree
     */
    void enterChronixTypedFunctions(CQLCFParser.ChronixTypedFunctionsContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#chronixTypedFunctions}.
     *
     * @param ctx the parse tree
     */
    void exitChronixTypedFunctions(CQLCFParser.ChronixTypedFunctionsContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLCFParser#chronixTypedFunction}.
     *
     * @param ctx the parse tree
     */
    void enterChronixTypedFunction(CQLCFParser.ChronixTypedFunctionContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#chronixTypedFunction}.
     *
     * @param ctx the parse tree
     */
    void exitChronixTypedFunction(CQLCFParser.ChronixTypedFunctionContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLCFParser#chronixfunction}.
     *
     * @param ctx the parse tree
     */
    void enterChronixfunction(CQLCFParser.ChronixfunctionContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#chronixfunction}.
     *
     * @param ctx the parse tree
     */
    void exitChronixfunction(CQLCFParser.ChronixfunctionContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLCFParser#name}.
     *
     * @param ctx the parse tree
     */
    void enterName(CQLCFParser.NameContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#name}.
     *
     * @param ctx the parse tree
     */
    void exitName(CQLCFParser.NameContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLCFParser#parameter}.
     *
     * @param ctx the parse tree
     */
    void enterParameter(CQLCFParser.ParameterContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#parameter}.
     *
     * @param ctx the parse tree
     */
    void exitParameter(CQLCFParser.ParameterContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLCFParser#chronixType}.
     *
     * @param ctx the parse tree
     */
    void enterChronixType(CQLCFParser.ChronixTypeContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLCFParser#chronixType}.
     *
     * @param ctx the parse tree
     */
    void exitChronixType(CQLCFParser.ChronixTypeContext ctx);
}