/*
 * Copyright (C) 2018 QAware GmbH
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
package de.qaware.chronix.solr.type.metric.functions.transformation;

import de.qaware.chronix.server.functions.ChronixTransformation;
import de.qaware.chronix.server.functions.FunctionCtx;
import de.qaware.chronix.server.types.ChronixTimeSeries;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * The subtract transformation
 *
 * @author f.lautenschlager
 */
public final class Subtract implements ChronixTransformation<MetricTimeSeries> {

    private double value;

    /**
     * Subtracts the value from each time series value
     *
     * @param timeSeriesList the list with time series
     * @return the transformed time series
     */
    @Override
    public void execute(List<ChronixTimeSeries<MetricTimeSeries>> timeSeriesList, FunctionCtx functionCtx) {

        for (ChronixTimeSeries<MetricTimeSeries> chronixTimeSeries : timeSeriesList) {
            MetricTimeSeries timeSeries = chronixTimeSeries.getRawTimeSeries();
            long[] timestamps = timeSeries.getTimestampsAsArray();
            double[] values = timeSeries.getValuesAsArray();

            timeSeries.clear();

            for (int i = 0; i < values.length; i++) {
                values[i] -= value;
            }

            timeSeries.addAll(timestamps, values);
            functionCtx.add(this, chronixTimeSeries.getJoinKey());
        }
    }

    @Override
    public String getQueryName() {
        return "sub";
    }

    @Override
    public String getType() {
        return "metric";
    }

    /**
     * @param args the first value is the amount that is subtracted
     */
    @Override
    public void setArguments(String[] args) {
        this.value = Double.parseDouble(args[0]);
    }

    @Override
    public String[] getArguments() {
        return new String[]{"value=" + value};
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .toString();
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
        Subtract rhs = (Subtract) obj;
        return new EqualsBuilder()
                .append(this.value, rhs.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(value)
                .toHashCode();
    }
}
