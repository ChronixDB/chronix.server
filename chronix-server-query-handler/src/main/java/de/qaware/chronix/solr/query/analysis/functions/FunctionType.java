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
    NNDERIVATIVE;

    /**
     * Checks if the given type is a high level analysis
     *
     * @param type the function type
     * @return true if the analysis type is a high level analysis, otherwise false
     */
    public static boolean isAnalysis(FunctionType type) {
        return TREND == type || OUTLIER == type || FREQUENCY == type || FASTDTW == type;
    }

    /**
     * Check if the given type is an aggregation
     *
     * @param type the function type
     * @return true if an aggregation, otherwise false
     */
    public static boolean isAggregation(FunctionType type) {
        return !isAnalysis(type) && !isTransformation(type);
    }

    /**
     * Checks if the given type is a transformation
     *
     * @param type the function type
     * @return true if the type is a transformation, otherwise false
     */
    public static boolean isTransformation(FunctionType type) {
        return VECTOR == type || DIVIDE == type || SCALE == type || BOTTOM == type || TOP == type || MOVAVG == type || DERIVATIVE == type || NNDERIVATIVE == type;
    }

}
