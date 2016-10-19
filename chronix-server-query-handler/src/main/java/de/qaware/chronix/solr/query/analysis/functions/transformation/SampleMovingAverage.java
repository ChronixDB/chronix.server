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
 * The sample moving average transformation.
 *
 * @author f.lautenschlager
 */
public final class SampleMovingAverage implements ChronixTransformation<MetricTimeSeries> {

    private final int samples;

    /**
     * Constructs a moving average transformation based on a fixed samples amount per window
     *
     * @param samples the amount of samples within a sliding window
     */
    public SampleMovingAverage(int samples) {
        this.samples = samples;
    }

    /**
     * Transforms a time series using a moving average that is based on a window with a fixed amount of samples.
     * The last window contains equals or a lower amount samples.
     *
     * @param timeSeries the time series that is transformed
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        //we need a sorted time series
        timeSeries.sort();

        //get the raw values as arrays
        double[] values = timeSeries.getValuesAsArray();
        long[] times = timeSeries.getTimestampsAsArray();

        int timeSeriesSize = timeSeries.size();
        //remove the old values
        timeSeries.clear();

        //the start is already set
        for (int start = 0; start < timeSeriesSize; start++) {

            int end = start + samples;
            //calculate the average of the values and the time
            evaluteAveragesAndAddToTimeSeries(timeSeries, values, times, start, end);

            //check if window end is larger than time series
            if (end + 1 >= timeSeriesSize) {
                evaluteAveragesAndAddToTimeSeries(timeSeries, values, times, start + 1, timeSeriesSize);
                break;
            }
        }

        functionValueMap.add(this);
    }

    /**
     * Calculates the average time stamp and value for the given window (start, end) and adds it to the given time series
     *
     * @param timeSeries the time series to add the moving averages
     * @param values     the values
     * @param times      the time stamps
     * @param startIdx   the start index of the window
     * @param end        the end index of the window
     */
    private void evaluteAveragesAndAddToTimeSeries(MetricTimeSeries timeSeries, double[] values, long[] times, int startIdx, int end) {

        //If the indices are equals, just return the value at the index position
        if (startIdx == end) {
            timeSeries.add(times[startIdx], values[startIdx]);
        }

        double valueSum = 0;
        long timeSum = 0;


        for (int i = startIdx; i < end; i++) {
            valueSum += values[i];
            timeSum += times[i];
        }
        int amount = end - startIdx;

        timeSeries.add(timeSum / amount, valueSum / amount);
    }


    @Override
    public FunctionType getType() {
        return FunctionType.SMOVAVG;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"samples=" + samples};
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("samples", samples)
                .toString();
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
        SampleMovingAverage rhs = (SampleMovingAverage) obj;
        return new EqualsBuilder()
                .append(this.samples, rhs.samples)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(samples)
                .toHashCode();
    }
}
