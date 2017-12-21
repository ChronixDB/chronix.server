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
package de.qaware.chronix.solr.type.metric.functions.aggregations;

import de.qaware.chronix.server.functions.ChronixAggregation;
import de.qaware.chronix.server.functions.FunctionCtx;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Sum aggregation for a time series
 *
 * @author f.lautenschlager
 */
public final class Sum implements ChronixAggregation<MetricTimeSeries> {
    /**
     * Calculates the sum of the values of the given time series
     *
     * @param timeSeriesList a list with time series
     * @return the sum of the values
     */
    @Override
    public void execute(List<ChronixTimeSeries<MetricTimeSeries>> timeSeriesList, FunctionCtx functionCtx) {
        for (ChronixTimeSeries<MetricTimeSeries> chronixTimeSeries : timeSeriesList) {

            MetricTimeSeries timeSeries = chronixTimeSeries.getRawTimeSeries();

            //If it is empty, we return NaN
            if (timeSeries.size() <= 0) {
                functionCtx.add(this, Double.NaN, chronixTimeSeries.getJoinKey());
                return;
            }

            //Else calculate the analysis value
            int size = timeSeries.size();
            double sum = 0;
            //Sum up the single values
            for (int i = 0; i < size; i++) {
                sum += timeSeries.getValue(i);

            }
            //return it
            functionCtx.add(this, sum, chronixTimeSeries.getJoinKey();
        }
    }

    @Override
    public String getQueryName() {
        return "sum";
    }

    @Override
    public String getType() {
        return "metric";
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
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }
}
