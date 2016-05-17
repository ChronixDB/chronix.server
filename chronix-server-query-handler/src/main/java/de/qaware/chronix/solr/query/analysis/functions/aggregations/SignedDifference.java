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

import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The signed difference (sdiff) aggregation returns the difference between the first and the last value.
 * It could be negative in that case, when the last value is below the first.
 *
 * @author f.lautenschlager
 */
public class SignedDifference implements ChronixAnalysis<MetricTimeSeries> {

    /**
     * Calculate the difference between the first and the last value of a given time series
     *
     * @param args the time series
     * @return the average or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {

        //Sum needs at least one time series
        if (args.length < 1) {
            throw new IllegalArgumentException("Signed difference function needs at least one time series");
        }
        //Took the first time series
        MetricTimeSeries timeSeries = args[0];

        //If it is empty, we return NaN
        if (timeSeries.size() <= 0) {
            return Double.NaN;
        }

        //we need to sort the time series
        timeSeries.sort();
        //get the first and the last value
        double first = timeSeries.getValue(0);
        double last = timeSeries.getValue(timeSeries.size() - 1);

        //both values are negative
        if (first < 0 && last < 0) {
            return last - first;
        }

        //both value are positive
        if (first > 0 && last > 0) {
            return last - first;
        }

        //start is negative and end is positive
        if (first < 0 && last > 0) {
            return last - first;
        }

        //start is positive and end is negative
        return last - first;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public FunctionType getType() {
        return FunctionType.SDIFF;
    }

    @Override
    public boolean needSubquery() {
        return false;
    }

    @Override
    public String getSubquery() {
        return null;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        SignedDifference rhs = (SignedDifference) obj;
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }
}
