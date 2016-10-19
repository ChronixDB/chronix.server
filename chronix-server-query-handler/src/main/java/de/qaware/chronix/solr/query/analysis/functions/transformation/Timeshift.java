/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap;
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
     * @param amount the amount, e.g 10
     * @param unit   the unit, e.g HOURS
     */
    public Timeshift(long amount, ChronoUnit unit) {
        this.amount = amount;
        this.unit = unit;
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
