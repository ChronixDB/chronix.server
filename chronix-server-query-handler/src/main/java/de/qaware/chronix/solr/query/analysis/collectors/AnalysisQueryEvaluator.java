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
package de.qaware.chronix.solr.query.analysis.collectors;

import de.qaware.chronix.solr.query.ChronixQueryParams;
import de.qaware.chronix.solr.query.analysis.collectors.aggregation.*;
import de.qaware.chronix.solr.query.analysis.collectors.analysis.FastDtw;
import de.qaware.chronix.solr.query.analysis.collectors.analysis.Frequency;
import de.qaware.chronix.solr.query.analysis.collectors.analysis.Outlier;
import de.qaware.chronix.solr.query.analysis.collectors.analysis.Trend;

import java.lang.reflect.MalformedParametersException;

/**
 * @author f.lautenschlager
 */
public final class AnalysisQueryEvaluator {

    private static final String AGGREGATION_DELIMITER = "=";
    private static final String AGGREGATION_ARGUMENT_DELIMITER = ":";
    private static final String AGGREGATION_ARGUMENT_SPLITTER = ",";


    private AnalysisQueryEvaluator() {
        //avoid instances
    }

    /**
     * Get the analysis and its argument.
     * An analysis is marked with one of the following strings
     * - ag=max    // maximum isAggregation
     * - ag=p:0.25 // 25% percentile
     * - analysis=trend // trend analysis. positive trend
     *
     * @param filterQueries - the filter queries of the user query
     * @return an entry containing the isAggregation and an isAggregation argument
     */
    public static ChronixAnalysis buildAnalysis(String[] filterQueries) {

        String unmodifiedAnalysis = getAnalysis(filterQueries);

        String aggregation = extractAggregation(unmodifiedAnalysis);
        String[] arguments = new String[0];
        //Aggregation has an argument
        if (aggregation.contains(AGGREGATION_ARGUMENT_DELIMITER)) {
            arguments = extractAggregationParameter(aggregation);
            aggregation = aggregation.substring(0, aggregation.indexOf(AGGREGATION_ARGUMENT_DELIMITER));
        }
        return getImplementation(AnalysisType.valueOf(aggregation.toUpperCase()), arguments);
    }

    private static ChronixAnalysis getImplementation(AnalysisType type, String[] arguments) {

        switch (type) {
            case AVG:
                return new Avg();
            case MIN:
                return new Min();
            case MAX:
                return new Max();
            case DEV:
                return new StdDev();
            case P:
                double p = Double.parseDouble(arguments[0]);
                return new Percentile(p);
            case TREND:
                return new Trend();
            case OUTLIER:
                return new Outlier();
            case FREQUENCY:
                long windowSize = Long.parseLong(arguments[0]);
                long windowThreshold = Long.parseLong(arguments[1]);
                return new Frequency(windowSize, windowThreshold);
            case FASTDTW:
                String subquery = arguments[0];
                int searchRadius = Integer.parseInt(arguments[1]);
                double maxAvgWarpingCost = Double.parseDouble(arguments[2]);
                return new FastDtw(subquery, searchRadius, maxAvgWarpingCost);

            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "Type: " + type + " not present.");
        }
    }

    private static String[] extractAggregationParameter(String argumentString) {
        String arguments = extractArguments(argumentString);
        return arguments.split(AGGREGATION_ARGUMENT_SPLITTER);
    }

    private static String extractArguments(String argumentString) {
        return extract(argumentString, AGGREGATION_ARGUMENT_DELIMITER);
    }


    private static String extractAggregation(String unmodifiedAggregation) {
        return extract(unmodifiedAggregation, AGGREGATION_DELIMITER);
    }

    private static String extract(String argumentString, String aggregationArgumentDelimiter) {
        int index = argumentString.indexOf(aggregationArgumentDelimiter);
        return argumentString.substring(index + 1);
    }

    private static String getAnalysis(String[] fqs) {
        if (fqs == null) {
            throw new MalformedParametersException("Aggregation must not null.");
        }

        for (String filterQuery : fqs) {
            if (filterQuery.startsWith(ChronixQueryParams.AGGREGATION_PARAM)) {
                return filterQuery;
            }
            if (filterQuery.startsWith(ChronixQueryParams.ANALYSIS_PARAM)) {
                return filterQuery;
            }
        }
        throw new MalformedParametersException("Aggregation must not empty.");

    }
}
