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
package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
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
public class Timeshift implements ChronixTransformation<MetricTimeSeries> {

    private final ChronoUnit unit;
    private final long amount;
    private final long shift;

    /**
     * Constructs a timeshift transformation
     *
     * @param amount the amount, e.g 10
     * @param unit   the unit, e.g HOURS
     */
    public Timeshift(long amount, ChronoUnit unit) {
        this.amount = amount;
        this.unit = unit;
        this.shift = unit.getDuration().toMillis() * amount;
    }

    @Override
    public void transform(MetricTimeSeries timeSeries) {
        double[] values = timeSeries.getValuesAsArray();
        long[] times = timeSeries.getTimestampsAsArray();

        timeSeries.clear();

        for (int i = 0; i < times.length; i++) {
            times[i] += shift;
        }

        timeSeries.addAll(times, values);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.TIMESHIFT;
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
