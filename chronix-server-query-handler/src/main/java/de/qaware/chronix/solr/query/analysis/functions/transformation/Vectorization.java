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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;

/**
 * This transformation does a vectorization of the time series by removing some points.
 *
 * @author f.lautenschlager
 */
public final class Vectorization implements ChronixTransformation<MetricTimeSeries> {

    private final float tolerance;

    /**
     * Constructs the vectorization transformation.
     * <p>
     * A typical tolerance value is 0.01f
     *
     * @param tolerance the value that is used to decide if the distance of values is almost equals.
     */
    public Vectorization(float tolerance) {
        this.tolerance = tolerance;
    }


    /**
     * Todo: Describe the algorithm, a bit.
     * <p>
     * Note: The transformation changes the values of the time series!
     * Further analyses such as aggregations uses the transformed values for the calculation.
     *
     * @param timeSeries the time series that is transformed
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        //we need a sorted time series
        timeSeries.sort();

        int size = timeSeries.size();
        //do not simplify if there are insufficient data points
        if (size <= 3) {
            return;
        }

        byte[] usePoint = new byte[size];
        Arrays.fill(usePoint, (byte) 1);

        long[] rawTimeStamps = timeSeries.getTimestampsAsArray();
        double[] rawValues = timeSeries.getValuesAsArray();

        //Clear the original time series
        timeSeries.clear();

        compute(rawTimeStamps, rawValues, usePoint, tolerance);

        for (int i = 0; i < size; i++) {
            if (usePoint[i] == 1) {
                timeSeries.add(rawTimeStamps[i], rawValues[i]);
            }
        }
        functionValueMap.add(this);
    }

    /**
     * Calculates the distance between a point and a line.
     * The distance function is defined as:
     * <p>
     * <code>
     * (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)</p>
     * s = -----------------------------</p>
     * L^2</p>
     * </code>
     * Then the distance from C to P = |s|*L.
     */
    private double getDistance(long pX, double pY, long aX, double aY, long bX, double bY) {

        double l2 = (bX - aX) * (bX - aX) + (bY - aY) * (bY - aY);
        double s = ((aY - pY) * (bX - aX) - (aX - pX) * (bY - aY)) / (l2);

        return Math.abs(s) * Math.sqrt(l2);
    }

    private void compute(long[] timestamps, double[] values, byte[] usePoint, float tolerance) {

        int ixA = 0;
        int ixB = 1;
        for (int i = 2; i < timestamps.length; i++) {
            double dist = getDistance(timestamps[i], values[i], timestamps[ixA], values[ixA], timestamps[ixB], values[ixB]);

            if (dist < tolerance) {
                usePoint[i - 1] = 0;
                continue;
            }
            //reached the end
            if (i + 1 == timestamps.length) {
                return;
            }
            // continue with next point
            ixA = i;
            ixB = i + 1;
            i++;
        }
    }

    @Override
    public FunctionType getType() {
        return FunctionType.VECTOR;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"tolerance=" + tolerance};
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
        Vectorization rhs = (Vectorization) obj;
        return new EqualsBuilder()
                .append(this.tolerance, rhs.tolerance)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(tolerance)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tolerance", tolerance)
                .toString();
    }
}
