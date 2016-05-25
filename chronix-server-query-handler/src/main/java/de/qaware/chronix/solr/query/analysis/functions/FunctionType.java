/*
 * Copyright (C) 2016 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The current implemented aggregations
 *
 * @author f.lautenschlager
 */
public enum FunctionType {

    //Aggregations
    AVG,
    MIN,
    MAX,
    DEV,
    P,
    SUM,
    COUNT,
    FIRST,
    LAST,
    RANGE,
    DIFF,
    SDIFF,
    //Analysis
    TREND,
    OUTLIER,
    FREQUENCY,
    FASTDTW,
    //Transformations
    VECTOR,
    DIVIDE,
    SCALE,
    BOTTOM,
    TOP,
    MOVAVG,
    DERIVATIVE,
    NNDERIVATIVE,
    ADD,
    SUB;

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionType.class);

    //Sets to hold the aggregations,analyses and transformations.
    //Otherwise the complexity of if(type == X || type == X ...) is to high
    private static final Set<FunctionType> aggregations = new HashSet<>();
    private static final Set<FunctionType> analyses = new HashSet<>();
    private static final Set<FunctionType> transformations = new HashSet<>();

    static {
        Collections.addAll(aggregations, AVG, MIN, MAX, DEV, P, SUM, COUNT, FIRST, LAST, RANGE, DIFF, SDIFF);
        Collections.addAll(analyses, TREND, OUTLIER, FREQUENCY, FASTDTW);
        Collections.addAll(transformations, VECTOR, DIVIDE, SCALE, BOTTOM, TOP, MOVAVG, DERIVATIVE, NNDERIVATIVE, ADD, SUB);

        if (aggregations.size() + analyses.size() + transformations.size() != FunctionType.values().length) {
            LOGGER.warn("Not all functions are added to the collections. There are more functions defined.");
        }
    }

    /**
     * Checks if the given type is a high level analysis
     *
     * @param type the function type
     * @return true if the analysis type is a high level analysis, otherwise false
     */
    public static boolean isAnalysis(FunctionType type) {
        return analyses.contains(type);
    }

    /**
     * Check if the given type is an aggregation
     *
     * @param type the function type
     * @return true if an aggregation, otherwise false
     */
    public static boolean isAggregation(FunctionType type) {
        return aggregations.contains(type);
    }

    /**
     * Checks if the given type is a transformation
     *
     * @param type the function type
     * @return true if the type is a transformation, otherwise false
     */
    public static boolean isTransformation(FunctionType type) {
        return transformations.contains(type);
    }

}
