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
 * The range analysis returns the difference between the maximum and minimum of a time series
 *
 * @author f.lautenschlager
 */
public final class Range implements ChronixAggregation<MetricTimeSeries> {

    /**
     * Gets difference between the maximum and the minimum value.
     * It is always a positive value.
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

        //the values to iterate
        double[] values = timeSeries.getValuesAsArray();
        //Initialize the values with the first element
        double min = values[0];
        double max = values[0];

        for (int i = 1; i < values.length; i++) {
            double current = values[i];

            //check for min
            if (current < min) {
                min = current;
            }
            //check of max
            if (current > max) {
                max = current;
            }
        }
        //return the absolute difference
        functionValueMap.add(this, Math.abs(max - min));
    }

    @Override
    public String[] getArguments() {
        return new String[0];
    }

    @Override
    public FunctionType getType() {
        return FunctionType.RANGE;
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
