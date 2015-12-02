/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis.aggregation.aggregator;

import java.lang.reflect.MalformedParametersException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author f.lautenschlager
 */
public class AggregationQueryEvaluator {

    private AggregationQueryEvaluator() {
        //avoid instances
    }

    /**
     * Get the aggregation and its argument
     *
     * @param filterQueries - the filter queries of the user query
     * @return an entry containing the aggregation and an aggregation argument
     */
    public static Map.Entry<AggregationType, Double> buildAggregation(String[] filterQueries) {

        String unmodifiedAggregation = getAggregation(filterQueries);

        String aggregation = extractAggregation(unmodifiedAggregation);
        double aggregationArgument = 0;
        //Aggregation has an argument
        if (aggregation.contains("=")) {
            aggregationArgument = extractAggregationParameter(aggregation);
            aggregation = aggregation.substring(0, aggregation.indexOf("="));
        }

        return new HashMap.SimpleEntry<>(AggregationType.valueOf(aggregation.toUpperCase()), aggregationArgument);

    }

    private static double extractAggregationParameter(String aggregation) {
        String parameter = extractAggregation(aggregation);
        if (!isNumeric(parameter)) {
            throw new MalformedParametersException("Aggregation parameter is not a numeric value: " + parameter);
        }
        return Double.valueOf(parameter);
    }

    private static String extractAggregation(String unmodifiedAggregation) {
        int index = unmodifiedAggregation.indexOf("=");
        return unmodifiedAggregation.substring(index + 1);
    }

    private static String getAggregation(String[] fqs) {
        if (fqs == null) {
            throw new MalformedParametersException("Aggregation must not null.");
        }

        for (String filterQuery : fqs) {
            if (filterQuery.startsWith("ag=")) {
                return filterQuery;
            }
        }
        throw new MalformedParametersException("Aggregation must not empty.");

    }

    private static boolean isNumeric(String str) {
        if (str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (c != '.' && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
