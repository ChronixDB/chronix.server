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
import de.qaware.chronix.timeseries.dt.DoubleList;
import de.qaware.chronix.timeseries.dt.LongList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This transformation does a vectorization of the time series by removing some points.
 *
 * @author f.lautenschlager
 */
public class Vectorization implements ChronixTransformation<MetricTimeSeries> {


    private final float TOLERANCE;

    /**
     * Constructs the vectorization transformation.
     * <p>
     * A typical tolerance value is 0.01f
     *
     * @param tolerance the value that is used to decide if the distance of values is almost equals.
     */
    public Vectorization(float tolerance) {
        this.TOLERANCE = tolerance;
    }

    /**
     * Todo: Describe the algorithm, a bit.
     * <p>
     * Note: The transformation changes the values of the time series!
     * Further analyses such as aggregations uses the transformed values for the calculation.
     *
     * @param timeSeries the time series that is transformed
     * @return a vectorized time series
     */
    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {

        int size = timeSeries.size();
        byte[] usePoint = new byte[size];

        long[] rawTimeStamps = timeSeries.getTimestampsAsArray();
        double[] rawValues = timeSeries.getValuesAsArray();

        compute(rawTimeStamps, rawValues, usePoint, TOLERANCE);

        LongList vectorizedTimeStamps = new LongList();
        DoubleList vectorizedValues = new DoubleList();
        for (int i = 0; i < size; i++) {
            //Value is not vectorized
            if (usePoint[i] != 0) {
                vectorizedTimeStamps.add(rawTimeStamps[i]);
                vectorizedValues.add(rawValues[i]);
            }
        }

        return new MetricTimeSeries.Builder(timeSeries.getMetric())
                .attributes(timeSeries.attributes())
                .points(vectorizedTimeStamps, vectorizedValues)
                .build();
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

        double l_2 = (bX - aX) * (bX - aX) + (bY - aY) * (bY - aY);
        double s = ((aY - pY) * (bX - aX) - (aX - pX) * (bY - aY)) / (l_2);

        return Math.abs(s) * Math.sqrt(l_2);
    }

    private void compute(long[] timestamps, double[] values, byte[] usePoint, float tolerance) {

        // do not simplify if not at least 3 points are available
        if (timestamps.length == 3) {
            return;
        }

        int ixA = 0;
        int ixB = 1;
        usePoint[ixA] = 1;
        usePoint[ixB] = 1;
        for (int i = 2; i < timestamps.length; i++) {
            double dist = getDistance(timestamps[i], values[i], timestamps[ixA], values[ixA], timestamps[ixB], values[ixB]);
            if (dist < tolerance) {
                usePoint[i - 1] = 0;
                usePoint[i] = 1;
            } else {

                // do not continue if not at least one more point is available
                if (i + 1 >= timestamps.length) {
                    for (int j = i; j < timestamps.length; j++) {
                        usePoint[j] = 1;
                    }
                    return;
                }

                ixA = i;
                ixB = i + 1;
                usePoint[ixA] = 1;
                usePoint[ixB] = 1;
                i++; // continue with next point
            }
        }
    }

    @Override
    public FunctionType getType() {
        return FunctionType.VECTOR;
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
