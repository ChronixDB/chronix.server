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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAggregation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Percentile aggregation analysis
 *
 * @author f.lautenschlager
 */
public final class Percentile implements ChronixAggregation<MetricTimeSeries> {

    private final double percentile;

    /**
     * Constructs a percentile aggregation
     *
     * @param percentile the percentile [0.0 ... 1.0]
     */
    public Percentile(double percentile) {
        this.percentile = percentile;
    }


    /**
     * Calculates the percentile of the first time series.
     *
     * @param timeSeries the time series
     * @return the percentile or 0 if the list is empty
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap analysisAndValues) {
        //If it is empty, we return NaN
        if (timeSeries.size() <= 0) {
            analysisAndValues.add(this, Double.NaN);
            return;
        }

        //Else calculate the analysis value
        analysisAndValues.add(this, de.qaware.chronix.solr.query.analysis.functions.math.Percentile.evaluate(timeSeries.getValues(), percentile));
    }

    @Override
    public String[] getArguments() {
        return new String[]{"percentile=" + percentile};
    }

    @Override
    public FunctionType getType() {
        return FunctionType.P;
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
        Percentile rhs = (Percentile) obj;
        return new EqualsBuilder()
                .append(this.percentile, rhs.percentile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(percentile)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("percentile", percentile)
                .toString();
    }
}
