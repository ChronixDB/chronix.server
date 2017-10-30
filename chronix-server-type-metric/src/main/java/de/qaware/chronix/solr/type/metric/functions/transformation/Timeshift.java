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
package de.qaware.chronix.solr.type.metric.functions.transformation;

import de.qaware.chronix.server.functions.ChronixTransformation;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.temporal.ChronoUnit;

/**
 * Shifts a time series by a given amount and unit, e.g. 2 hours.
 *
 * @author f.lautenschlager
 */
public final class Timeshift implements ChronixTransformation<MetricTimeSeries> {

    private final ChronoUnit unit;
    private final long amount;
    private final long shift;

    /**
     * Constructs a timeshift transformation
     *
     * @param args the first value is the amount, e.g 10, the second one is the unit, e.g HOURS
     */
    public Timeshift(String[] args) {
        this.amount = Long.parseLong(args[0]);
        this.unit = ChronoUnit.valueOf(args[1].toUpperCase());
        this.shift = unit.getDuration().toMillis() * amount;
    }

    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {
        double[] values = timeSeries.getValuesAsArray();
        long[] times = timeSeries.getTimestampsAsArray();

        timeSeries.clear();

        for (int i = 0; i < times.length; i++) {
            times[i] += shift;
        }

        timeSeries.addAll(times, values);
        functionValueMap.add(this);
    }

    @Override
    public String getQueryName() {
        return "timeshift";
    }

    @Override
    public String getTimeSeriesType() {
        return "metric";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"amount=" + amount, "unit=" + unit.name().toUpperCase()};
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
        Timeshift rhs = (Timeshift) obj;
        return new EqualsBuilder()
                .append(this.unit, rhs.unit)
                .append(this.amount, rhs.amount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(unit)
                .append(amount)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("amount", amount)
                .append("unit", unit)
                .append("shift", shift)
                .toString();
    }
}
