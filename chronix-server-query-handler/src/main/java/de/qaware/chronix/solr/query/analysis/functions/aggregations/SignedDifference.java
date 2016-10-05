/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.aggregations;

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAggregation;
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
public final class SignedDifference implements ChronixAggregation<MetricTimeSeries> {

    /**
     * Calculate the difference between the first and the last value of a given time series
     *
     * @param timeSeries the time series
     * @return the average or 0 if the list is empty
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {
        //If it is empty, we return NaN
        if (timeSeries.size() <= 0) {
            functionValueMap.add(this, Double.NaN);
            return;
        }

        //we need to sort the time series
        timeSeries.sort();
        //get the first and the last value
        double first = timeSeries.getValue(0);
        double last = timeSeries.getValue(timeSeries.size() - 1);

        //both values are negative
        if (first < 0 && last < 0) {
            functionValueMap.add(this, last - first);
            return;
        }

        //both value are positive
        if (first > 0 && last > 0) {
            functionValueMap.add(this, last - first);
            return;
        }

        //start is negative and end is positive
        if (first < 0 && last > 0) {
            functionValueMap.add(this, last - first);
            return;
        }

        //start is positive and end is negative
        functionValueMap.add(this, last - first);
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
