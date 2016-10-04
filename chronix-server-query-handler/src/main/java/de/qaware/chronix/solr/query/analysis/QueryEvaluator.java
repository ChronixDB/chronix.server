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

import de.qaware.chronix.solr.query.ChronixQueryParams;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.aggregations.*;
import de.qaware.chronix.solr.query.analysis.functions.analyses.FastDtw;
import de.qaware.chronix.solr.query.analysis.functions.analyses.Frequency;
import de.qaware.chronix.solr.query.analysis.functions.analyses.Outlier;
import de.qaware.chronix.solr.query.analysis.functions.analyses.Trend;
import de.qaware.chronix.solr.query.analysis.functions.transformation.*;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;

/**
 * @author f.lautenschlager
 */
public final class QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvaluator.class);

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
    public static QueryFunctions extractFunctions(String[] filterQueries) {

        //The result that contains the asked analyses
        final QueryFunctions result = new QueryFunctions();
        //Check if there are filter queries with functions
        if (isEmpty(filterQueries)) {
            //return a empty result
            return result;
        }

        //Placeholder for arguments
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
                FunctionType type = FunctionType.valueOf(subFunction.toUpperCase());

                //add the implementation of the asked functions
                addFunction(result, type, arguments);

            }
        }

        return result;
    }

    /**
     * Helper to check if the given string array is empty.
     * A string is empty if it is null, or ""
     *
     * @param fqs the string array
     * @return true if empty, otherwise false
     */
    private static boolean isEmpty(String[] fqs) {
        if (fqs == null || fqs.length == 0) {
            return true;
        }

        for (String fq : fqs) {
            if (!StringUtils.isEmpty(fq) && fq.startsWith(ChronixQueryParams.FUNCTION)) {
                return false;
            }
        }
        return true;
    }

    private static void addFunction(QueryFunctions result, FunctionType type, String[] arguments) {

        if (FunctionType.isTransformation(type)) {
            //Check if the type is a transformation and add it
            addTransformation(result, type, arguments);
        } else if (FunctionType.isAnalysis(type)) {
            //Check if the type is an analysis and add it
            addAnalysis(result, type, arguments);
        } else if (FunctionType.isAggregation(type)) {
            //Check if the type is an aggregation and add it
            addAggregation(result, type, arguments);
        } else if (FunctionType.isLsof(type)) {
            addLsof(result, type, arguments);
        } else if (FunctionType.isStrace(type)) {
            addStrace(result, type, arguments);
        } else {
            LOGGER.info("{} is unknown. {} is ignored", type, type);
        }
    }

    private static void addStrace(QueryFunctions result, FunctionType type, String[] arguments) {
        switch (type) {
            case SPLIT:
                result.addTransformation(new Split());
                break;
        }
    }

    private static void addLsof(QueryFunctions result, FunctionType type, String[] arguments) {
        switch (type) {
            case GROUP:
                String[] filters = new String[arguments.length - 1];
                System.arraycopy(arguments, 1, filters, 0, filters.length);
                result.addTransformation(new Group(arguments[0], filters));
                break;
        }
    }

    private static void addAggregation(QueryFunctions result, FunctionType type, String[] arguments) {
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
            case INTEGRAL:
                result.addAggregation(new Integral());
                break;
            default:
                LOGGER.warn("Ignoring {} as an aggregation. {} is unknown", type, type);
        }
    }

    private static void addAnalysis(QueryFunctions result, FunctionType type, String[] arguments) {
        switch (type) {
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
            default:
                LOGGER.warn("Ignoring {} as an analysis. {} is unknown", type, type);
        }
    }

    private static void addTransformation(QueryFunctions result, FunctionType type, String[] arguments) {
        switch (type) {
            //Transformations
            case ADD:
                double addValue = Double.parseDouble(arguments[0]);
                result.addTransformation(new Add(addValue));
                break;
            case SUB:
                double subValue = Double.parseDouble(arguments[0]);
                result.addTransformation(new Subtract(subValue));
                break;
            case VECTOR:
                float tolerance = Float.parseFloat(arguments[0]);
                result.addTransformation(new Vectorization(tolerance));
                break;
            case BOTTOM:
                int bottomN = Integer.parseInt(arguments[0]);
                result.addTransformation(new Bottom(bottomN));
                break;
            case TOP:
                int topN = Integer.parseInt(arguments[0]);
                result.addTransformation(new Top(topN));
                break;
            case MOVAVG:
                long timeSpan = Long.parseLong(arguments[0]);
                ChronoUnit unit = ChronoUnit.valueOf(arguments[1].toUpperCase());
                result.addTransformation(new MovingAverage(timeSpan, unit));
                break;
            case SCALE:
                double scale = Double.parseDouble(arguments[0]);
                result.addTransformation(new Scale(scale));
                break;
            case DIVIDE:
                double factor = Double.parseDouble(arguments[0]);
                result.addTransformation(new Divide(factor));
                break;
            case DERIVATIVE:
                result.addTransformation(new Derivative());
                break;
            case NNDERIVATIVE:
                result.addTransformation(new NonNegativeDerivative());
                break;
            case TIMESHIFT:
                long shiftAmount = Long.parseLong(arguments[0]);
                ChronoUnit shiftUnit = ChronoUnit.valueOf(arguments[1].toUpperCase());
                result.addTransformation(new Timeshift(shiftAmount, shiftUnit));
                break;
            case DISTINCT:
                result.addTransformation(new Distinct());
                break;
            default:
                LOGGER.warn("Ignoring {} as a transformation. {} is unknown", type, type);
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
