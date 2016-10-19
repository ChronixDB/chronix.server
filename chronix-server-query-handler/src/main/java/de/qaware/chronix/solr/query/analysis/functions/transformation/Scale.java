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

/**
 * Scale transformation
 *
 * @author f.lautenschlager
 */
public final class Scale implements ChronixTransformation<MetricTimeSeries> {

    private final double value;

    /**
     * Scales the time series by the given factor
     *
     * @param value the value factor
     */
    public Scale(double value) {
        this.value = value;
    }

    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        //get a copy of the values
        double[] values = timeSeries.getValuesAsArray();
        //get a copy of timestamps
        long[] times = timeSeries.getTimestampsAsArray();
        for (int i = 0; i < timeSeries.size(); i++) {
            //scale the original value
            values[i] = values[i] * value;
        }
        //clear and delete the time series
        timeSeries.clear();
        timeSeries.addAll(times, values);

        functionValueMap.add(this);

    }

    @Override
    public FunctionType getType() {
        return FunctionType.SCALE;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"value=" + value};
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
        Scale rhs = (Scale) obj;
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


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .toString();
    }
}
