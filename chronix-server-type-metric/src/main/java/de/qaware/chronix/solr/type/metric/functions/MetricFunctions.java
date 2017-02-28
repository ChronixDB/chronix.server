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
package de.qaware.chronix.solr.type.metric.functions;

import java.util.*;

/**
 * The current implemented AGGREGATIONS
 *
 * @author f.lautenschlager
 */
public class MetricFunctions {

    //Sets to hold the aggregations, analyses and transformations.
    //Otherwise the complexity of if(type == X || type == X ...) is to high
    private static final Set<String> AGGREGATIONS = new HashSet<>();
    private static final Set<String> ANALYSES = new HashSet<>();
    private static final Set<String> TRANSFORMATIONS = new HashSet<>();

    public static List<String> ALL_FUNCTIONS = new ArrayList<>();

    static {
        Collections.addAll(AGGREGATIONS, "AVG", "MIN", "MAX", "DEV", "P", "SUM", "COUNT", "FIRST", "LAST", "RANGE", "DIFF", "SDIFF", "INTEGRAL");
        Collections.addAll(ANALYSES, "TREND", "OUTLIER", "FREQUENCY", "FASTDTW");
        Collections.addAll(TRANSFORMATIONS, "VECTOR", "DIVIDE", "SCALE", "BOTTOM", "TOP", "MOVAVG", "SMOVAVG", "DERIVATIVE", "NNDERIVATIVE", "ADD", "SUB", "TIMESHIFT", "DISTINCT");

        Collections.addAll(ALL_FUNCTIONS, "AVG", "MIN", "MAX", "DEV", "P", "SUM", "COUNT", "FIRST", "LAST", "RANGE", "DIFF", "SDIFF", "INTEGRAL");
        Collections.addAll(ALL_FUNCTIONS, "TREND", "OUTLIER", "FREQUENCY", "FASTDTW");
        Collections.addAll(ALL_FUNCTIONS, "VECTOR", "DIVIDE", "SCALE", "BOTTOM", "TOP", "MOVAVG", "SMOVAVG", "DERIVATIVE", "NNDERIVATIVE", "ADD", "SUB", "TIMESHIFT", "DISTINCT");

    }

    /**
     * Checks if the given type is a high level analysis
     *
     * @param type the function type
     * @return true if the analysis type is a high level analysis, otherwise false
     */
    public static boolean isAnalysis(String type) {
        return ANALYSES.contains(type);
    }

    /**
     * Check if the given type is an aggregation
     *
     * @param type the function type
     * @return true if an aggregation, otherwise false
     */
    public static boolean isAggregation(String type) {
        return AGGREGATIONS.contains(type);
    }

    /**
     * Checks if the given type is a transformation
     *
     * @param type the function type
     * @return true if the type is a transformation, otherwise false
     */
    public static boolean isTransformation(String type) {
        return TRANSFORMATIONS.contains(type);
    }

}
