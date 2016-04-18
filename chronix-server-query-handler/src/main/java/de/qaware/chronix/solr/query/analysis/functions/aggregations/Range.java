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
package de.qaware.chronix.solr.query.analysis.functions.aggregations;

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * The range analysis returns the difference between the maximum and minimum of a time series
 *
 * @author f.lautenschlager
 */
public final class Range implements ChronixAnalysis {

    /**
     * Gets difference between the maximum and the minimum value.
     * It is always a positive value.
     *
     * @param args the time series
     * @return the average or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {

        //Sum needs at least one time series
        if (args.length < 1) {
            throw new IllegalArgumentException("Range function needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];

        //If it is empty, we return NaN
        if (timeSeries.size() <= 0) {
            return Double.NaN;
        }

        //the values to iterate
        double[] values = timeSeries.getValuesAsArray();
        //Initialize the values with the first element
        double min = values[0];
        double max = values[0];

        for (int i = 1; i < values.length; i++) {
            double current = values[i];

            //check for min
            if (current < min) {
                min = current;
            }
            //check of max
            if (current > max) {
                max = current;
            }
        }
        //return the absolute difference
        return Math.abs(max - min);
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.RANGE;
    }

    @Override
    public boolean needSubquery() {
        return false;
    }

    @Override
    public String getSubquery() {
        return null;
    }

}
