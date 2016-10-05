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

import de.qaware.chronix.converter.common.DoubleList;
import de.qaware.chronix.converter.common.LongList;
import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * The distinct transformation.
 *
 * @author f.lautenschlager
 */
public final class Distinct implements ChronixTransformation<MetricTimeSeries> {
    /**
     * Transforms a time series into a representation with distinct values.
     * The distinct operation uses the first occurrence of a point.
     *
     * @param timeSeries        the time series that is transformed
     * @param functionValueMap the function value map
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        if (timeSeries.isEmpty()) {
            return;
        }

        timeSeries.sort();

        LongList timeList = new LongList(timeSeries.size());
        DoubleList valueList = new DoubleList(timeSeries.size());

        //We should use a other data structure...
        Set<Double> distinct = new HashSet<>();

        for (int i = 0; i < timeSeries.size(); i++) {
            double value = timeSeries.getValue(i);

            if (!distinct.contains(value)) {
                timeList.add(timeSeries.getTime(i));
                valueList.add(value);
                distinct.add(value);
            }
        }
        timeSeries.clear();
        timeSeries.addAll(timeList, valueList);

        functionValueMap.add(this);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.DISTINCT;
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
