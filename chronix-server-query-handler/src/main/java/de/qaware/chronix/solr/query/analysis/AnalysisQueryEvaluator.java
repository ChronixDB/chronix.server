/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis;

import de.qaware.chronix.solr.query.ChronixQueryParams;
import de.qaware.chronix.solr.query.analysis.functions.*;

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
            case SAX:
                String regex = arguments[0];
                int paaSize = Integer.parseInt(arguments[1]);
                int alphabet = Integer.parseInt(arguments[2]);
                double threshold = Double.parseDouble(arguments[3]);
                return new Sax(regex, paaSize, alphabet, threshold);

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
