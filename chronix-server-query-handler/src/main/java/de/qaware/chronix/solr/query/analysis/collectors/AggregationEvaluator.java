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

import de.qaware.chronix.solr.query.analysis.collectors.math.Percentile;
import de.qaware.chronix.solr.query.analysis.collectors.math.StdDev;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.DoubleList;

/**
 * Aggregation evaluator supports AVG, MIN, max, dev and percentile
 *
 * @author f.lautenschlager
 */
public final class AggregationEvaluator {


    private AggregationEvaluator() {
        //avoid instances
    }

    /**
     * Evaluates the given isAggregation on the given time series
     *
     * @param timeSeries the time series
     * @param analysis   the analysis (avg, min, max, dev, p, trend, outlier, frequency) with its arguemnts
     * @return the aggregated value or in case of a high level analysis 1 for anomaly detected or -1 for not.
     */
    public static double aggregate(MetricTimeSeries timeSeries, ChronixAnalysis analysis) {
        double value;
        switch (analysis.getType()) {
            case AVG:
                value = avg(timeSeries.getValues());
                break;
            case MIN:
                value = min(timeSeries.getValues());
                break;
            case MAX:
                value = max(timeSeries.getValues());
                break;
            case DEV:
                value = StdDev.dev(timeSeries.getValues());
                break;
            case P:
                value = Percentile.evaluate(timeSeries.getValues(), Double.parseDouble(analysis.getArguments()[0]));
                break;
            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The high-level analysis " + analysis + " is not present within the enum.");
        }

        return value;
    }

    /**
     * Calculates the maximum value
     *
     * @param values the time series values
     * @return the maximum or 0 if the list is empty
     */
    private static double max(DoubleList values) {
        double current = 0;

        if (values.size() <= 0) {
            return current;
        }

        for (int i = 0; i < values.size(); i++) {

            double next = values.get(i);

            if (current < next) {
                current = next;
            }
        }
        return current;
    }

    /**
     * Calculates the minimum value
     *
     * @param values the time series values
     * @return the maximum or 0 if the list is empty
     */
    private static double min(DoubleList values) {
        double current = 0;

        if (values.size() <= 0) {
            return current;
        }

        for (int i = 0; i < values.size(); i++) {
            double next = values.get(i);

            if (current > next) {
                current = next;
            }
        }
        return current;
    }

    /**
     * Calculates the average value
     *
     * @param values the time series values
     * @return the maximum or 0 if the list is empty
     */
    private static double avg(DoubleList values) {
        double current = 0;
        double count = 0;
        for (int i = 0; i < values.size(); i++) {
            current += values.get(i);
            count++;
        }

        return current / count;
    }
}
