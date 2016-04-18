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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Percentile aggregation analysis
 *
 * @author f.lautenschlager
 */
public final class Percentile implements ChronixAnalysis {

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
     * @param args the time series
     * @return the percentile or 0 if the list is empty
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        //Sum needs at least one time series
        if (args.length < 1) {
            throw new IllegalArgumentException("Percentile aggregation needs at least one time series");
        }

        MetricTimeSeries timeSeries = args[0];

        //If it is empty, we return NaN
        if (timeSeries.size() <= 0) {
            return Double.NaN;
        }

        //Else calculate the analysis value
        return de.qaware.chronix.solr.query.analysis.functions.math.Percentile.evaluate(timeSeries.getValues(), percentile);
    }

    @Override
    public String[] getArguments() {
        return new String[]{"percentile=" + percentile};
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.P;
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
