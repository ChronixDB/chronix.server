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

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CQLParser}.
 */
public interface CQLListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link CQLParser#cql}.
     *
     * @param ctx the parse tree
     */
    void enterCql(CQLParser.CqlContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#cql}.
     *
     * @param ctx the parse tree
     */
    void exitCql(CQLParser.CqlContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixJoinParameter}.
     *
     * @param ctx the parse tree
     */
    void enterChronixJoinParameter(CQLParser.ChronixJoinParameterContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixJoinParameter}.
     *
     * @param ctx the parse tree
     */
    void exitChronixJoinParameter(CQLParser.ChronixJoinParameterContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixJoinFields}.
     *
     * @param ctx the parse tree
     */
    void enterChronixJoinFields(CQLParser.ChronixJoinFieldsContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixJoinFields}.
     *
     * @param ctx the parse tree
     */
    void exitChronixJoinFields(CQLParser.ChronixJoinFieldsContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixJoinField}.
     *
     * @param ctx the parse tree
     */
    void enterChronixJoinField(CQLParser.ChronixJoinFieldContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixJoinField}.
     *
     * @param ctx the parse tree
     */
    void exitChronixJoinField(CQLParser.ChronixJoinFieldContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixFunctionParameter}.
     *
     * @param ctx the parse tree
     */
    void enterChronixFunctionParameter(CQLParser.ChronixFunctionParameterContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixFunctionParameter}.
     *
     * @param ctx the parse tree
     */
    void exitChronixFunctionParameter(CQLParser.ChronixFunctionParameterContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixTypedFunctions}.
     *
     * @param ctx the parse tree
     */
    void enterChronixTypedFunctions(CQLParser.ChronixTypedFunctionsContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixTypedFunctions}.
     *
     * @param ctx the parse tree
     */
    void exitChronixTypedFunctions(CQLParser.ChronixTypedFunctionsContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixTypedFunction}.
     *
     * @param ctx the parse tree
     */
    void enterChronixTypedFunction(CQLParser.ChronixTypedFunctionContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixTypedFunction}.
     *
     * @param ctx the parse tree
     */
    void exitChronixTypedFunction(CQLParser.ChronixTypedFunctionContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixfunction}.
     *
     * @param ctx the parse tree
     */
    void enterChronixfunction(CQLParser.ChronixfunctionContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixfunction}.
     *
     * @param ctx the parse tree
     */
    void exitChronixfunction(CQLParser.ChronixfunctionContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#name}.
     *
     * @param ctx the parse tree
     */
    void enterName(CQLParser.NameContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#name}.
     *
     * @param ctx the parse tree
     */
    void exitName(CQLParser.NameContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#parameter}.
     *
     * @param ctx the parse tree
     */
    void enterParameter(CQLParser.ParameterContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#parameter}.
     *
     * @param ctx the parse tree
     */
    void exitParameter(CQLParser.ParameterContext ctx);

    /**
     * Enter a parse tree produced by {@link CQLParser#chronixType}.
     *
     * @param ctx the parse tree
     */
    void enterChronixType(CQLParser.ChronixTypeContext ctx);

    /**
     * Exit a parse tree produced by {@link CQLParser#chronixType}.
     *
     * @param ctx the parse tree
     */
    void exitChronixType(CQLParser.ChronixTypeContext ctx);
}