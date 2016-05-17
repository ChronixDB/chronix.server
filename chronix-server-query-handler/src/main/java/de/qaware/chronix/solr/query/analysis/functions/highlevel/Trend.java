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
package de.qaware.chronix.solr.query.analysis.functions.highlevel;

import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.math.LinearRegression;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The trend analysis
 *
 * @author f.lautenschlager
 */
public final class Trend implements ChronixAnalysis<MetricTimeSeries> {
    /**
     * Detects trends in time series using a linear regression.
     *
     * @param args the time series
     * @return 1 if there is a positive trend, otherwise -1
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length <= 0) {
            throw new IllegalArgumentException("Trend detection needs at least one time series");
        }
        //Took the first time series
        MetricTimeSeries timeSeries = args[0];
        //We need to sort the time series for this analysis
        timeSeries.sort();
        //Calculate the linear regression
        LinearRegression linearRegression = new LinearRegression(timeSeries.getTimestamps(), timeSeries.getValues());
        double slope = linearRegression.slope();
        //If we have a positive slope, we return 1 otherwise -1
        return slope > 0 ? 1 : -1;
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public FunctionType getType() {
        return FunctionType.TREND;
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
        Trend rhs = (Trend) obj;
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }
}
