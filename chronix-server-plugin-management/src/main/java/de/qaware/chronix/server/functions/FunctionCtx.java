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
package de.qaware.chronix.server.functions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple fixed size map of chronix analysis and value
 *
 * @author f.lautenschlager
 */
public class FunctionCtx {

    //TODO: Check if we need this.
    private final Map<String, FunctionCtxEntry> functionCtxEntries = Collections.synchronizedMap(new HashMap<>());

    private final int maxAmountOfAggregations;
    private final int maxAmountOfAnalyses;
    private final int maxAmountOfTransformations;


    /**
     * A container for analyses and its results
     *
     * @param amountOfAggregations    the number of aggregations
     * @param amountOfAnalyses        the number of analyses
     * @param amountOfTransformations the number of transformations
     */
    public FunctionCtx(int amountOfAggregations, int amountOfAnalyses, int amountOfTransformations) {
        this.maxAmountOfAggregations = amountOfAggregations;
        this.maxAmountOfAnalyses = amountOfAnalyses;
        this.maxAmountOfTransformations = amountOfTransformations;
    }

    /**
     * Appends the analysis to the result
     *
     * @param analysis the chronix analysis
     * @param value    the value for the analysis
     */
    public void add(ChronixAnalysis analysis, boolean value, String joinKey) {
        addEntryIfNotExist(joinKey);
        functionCtxEntries.get(joinKey).add(analysis, value);
    }

    /**
     * Append the aggregation to the result
     *
     * @param aggregation the chronix aggregation
     * @param value       the value of the aggregation
     */
    public void add(ChronixAggregation aggregation, double value, String joinKey) {
        addEntryIfNotExist(joinKey);
        functionCtxEntries.get(joinKey).add(aggregation, value);
    }

    /**
     * Appends the transformation to the result
     *
     * @param transformation add an transformation
     */
    public void add(ChronixTransformation transformation, String joinKey) {
        addEntryIfNotExist(joinKey);
        functionCtxEntries.get(joinKey).add(transformation);
    }

    private synchronized void addEntryIfNotExist(String joinKey) {
        if (!functionCtxEntries.containsKey(joinKey)) {
            functionCtxEntries.put(joinKey, new FunctionCtxEntry(maxAmountOfTransformations, maxAmountOfAggregations, maxAmountOfAnalyses));
        }
    }

    /**
     * @param joinKey used to create groups time series chunks
     * @return the entry in the function context for this time series
     */
    public FunctionCtxEntry getContextFor(String joinKey) {
        return functionCtxEntries.get(joinKey);
    }
}
