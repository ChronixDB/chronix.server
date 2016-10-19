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
import de.qaware.chronix.solr.query.analysis.functions.math.DerivativeUtil;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Non negative derivative transformation.
 * Like the derivative transformation but does not return negative values
 *
 * @author f.lautenschlager
 */
public final class NonNegativeDerivative implements ChronixTransformation<MetricTimeSeries> {
    /**
     * Calculates the derivative of the time series
     *
     * @param timeSeries the time series that is transformed
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {
        //we need a sorted time series
        timeSeries.sort();

        long[] times = timeSeries.getTimestampsAsArray();
        double[] values = timeSeries.getValuesAsArray();

        //Clear the time series
        timeSeries.clear();

        for (int i = 1; i < values.length - 1; i++) {

            long yT1 = times[i + 1];
            long yT0 = times[i - 1];

            double xT1 = values[i + 1];
            double xT0 = values[i - 1];

            double derivativeValue = DerivativeUtil.derivative(xT1, xT0, yT1, yT0);

            if (derivativeValue >= 0) {
                //We use the average time of
                long derivativeTime = yT1 + (yT1 - yT0) / 2;
                timeSeries.add(derivativeTime, derivativeValue);
            }
        }
        functionValueMap.add(this);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.NNDERIVATIVE;
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
