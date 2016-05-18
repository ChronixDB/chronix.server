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
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.aggregations.*;
import de.qaware.chronix.solr.query.analysis.functions.analyses.FastDtw;
import de.qaware.chronix.solr.query.analysis.functions.analyses.Frequency;
import de.qaware.chronix.solr.query.analysis.functions.analyses.Outlier;
import de.qaware.chronix.solr.query.analysis.functions.analyses.Trend;
import de.qaware.chronix.solr.query.analysis.functions.transformation.Vectorization;
import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.lang.reflect.MalformedParametersException;

/**
 * @author f.lautenschlager
 */
public final class QueryEvaluator {

    private static final String AGGREGATION_DELIMITER = "=";
    private static final String AGGREGATION_ARGUMENT_DELIMITER = ":";
    private static final String AGGREGATION_ARGUMENT_SPLITTER = ",";
    private static final String FUNCTION_ARGUMENT_SPLITTER = ";";


    private QueryEvaluator() {
        //avoid instances
    }

    /**
     * Analyzes the filter queries and parses them for chronix fucntions
     *
     * @param filterQueries the filter queries (solr api)
     * @return a set of chronix analyses asked in the filter queries
     */
    public static QueryFunctions<MetricTimeSeries> extractFunctions(String[] filterQueries) {

        if (filterQueries == null || filterQueries.length == 0) {
            throw new MalformedParametersException("Functions must not be null.");
        }
        //The result that contains the asked analyses
        QueryFunctions<MetricTimeSeries> result = new QueryFunctions<>();
        String[] arguments = new String[0];

        //Iterate over all filter queries
        for (String unmodifiedAnalysis : filterQueries) {
            //Get the plain function without '*='
            String function = extractFunction(unmodifiedAnalysis);

            String[] functions;
            //check if we have one or more functions / aggregations
            if (function.contains(FUNCTION_ARGUMENT_SPLITTER)) {
                functions = function.split(FUNCTION_ARGUMENT_SPLITTER);
            } else {
                functions = new String[]{function};
            }

            //run over the functions
            for (String subFunction : functions) {
                //function has an argument
                if (subFunction.contains(AGGREGATION_ARGUMENT_DELIMITER)) {
                    arguments = extractAggregationParameter(subFunction);
                    subFunction = subFunction.substring(0, subFunction.indexOf(AGGREGATION_ARGUMENT_DELIMITER));
                }
                //add the implementation of the asked functions
                addFunction(result, FunctionType.valueOf(subFunction.toUpperCase()), arguments);

            }
        }

        return result;
    }

    private static void addFunction(QueryFunctions<MetricTimeSeries> result, FunctionType type, String[] arguments) {

        switch (type) {
            //Aggregations
            case AVG:
                result.addAggregation(new Avg());
                break;
            case MIN:
                result.addAggregation(new Min());
                break;
            case MAX:
                result.addAggregation(new Max());
                break;
            case SUM:
                result.addAggregation(new Sum());
                break;
            case COUNT:
                result.addAggregation(new Count());
                break;
            case DEV:
                result.addAggregation(new StdDev());
                break;
            case LAST:
                result.addAggregation(new Last());
                break;
            case FIRST:
                result.addAggregation(new First());
                break;
            case RANGE:
                result.addAggregation(new Range());
                break;
            case DIFF:
                result.addAggregation(new Difference());
                break;
            case SDIFF:
                result.addAggregation(new SignedDifference());
                break;
            case P:
                double p = Double.parseDouble(arguments[0]);
                result.addAggregation(new Percentile(p));
                break;
            //Analyses
            case TREND:
                result.addAnalysis(new Trend());
                break;
            case OUTLIER:
                result.addAnalysis(new Outlier());
                break;
            case FREQUENCY:
                long windowSize = Long.parseLong(arguments[0]);
                long windowThreshold = Long.parseLong(arguments[1]);
                result.addAnalysis(new Frequency(windowSize, windowThreshold));
                break;
            case FASTDTW:
                String subquery = removeBrackets(arguments[0]);
                int searchRadius = Integer.parseInt(arguments[1]);
                double maxAvgWarpingCost = Double.parseDouble(arguments[2]);
                result.addAnalysis(new FastDtw(subquery, searchRadius, maxAvgWarpingCost));
                break;
            //Transformations
            case VECTOR:
                float tolerance = Float.parseFloat(arguments[0]);
                result.addTransformation(new Vectorization(tolerance));
                break;
            default:
                throw new EnumConstantNotPresentException(FunctionType.class, "Type: " + type + " not present.");
        }
    }

    /**
     * Removes the first and the last bracket from the sub-query.
     *
     * @param subQuery the sub-query for asking another set of time series
     * @return the sub-query without a leading and closing bracket.
     */
    private static String removeBrackets(String subQuery) {
        //remove the enfolding brackets
        if (subQuery.indexOf('(') == 0 && subQuery.lastIndexOf(')') == subQuery.length() - 1) {
            return subQuery.substring(1, subQuery.length() - 1);
        }
        return subQuery;
    }

    private static String[] extractAggregationParameter(String argumentString) {
        String arguments = extractArguments(argumentString);
        return arguments.split(AGGREGATION_ARGUMENT_SPLITTER);
    }

    private static String extractArguments(String argumentString) {
        return extract(argumentString, AGGREGATION_ARGUMENT_DELIMITER);
    }


    private static String extractFunction(String unmodifiedAggregation) {
        return extract(unmodifiedAggregation, AGGREGATION_DELIMITER);
    }

    private static String extract(String argumentString, String aggregationArgumentDelimiter) {
        int index = argumentString.indexOf(aggregationArgumentDelimiter);
        if (index == -1) {
            throw new IllegalStateException("Invalid query syntax. No delimiter '" + aggregationArgumentDelimiter + "' found");
        }
        return argumentString.substring(index + 1);
    }
}
