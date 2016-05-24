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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.temporal.ChronoUnit;

/**
 * The moving average transformation.
 *
 * @author f.lautenschlager
 */
public class MovingAverage implements ChronixTransformation<MetricTimeSeries> {

    private final long timeSpan;
    private final ChronoUnit unit;
    private final long windowTime;

    /**
     * Constructs a moving average transformation
     *
     * @param timeSpan the time span e.g. 5, 10
     * @param unit     the unit of the time span
     */
    public MovingAverage(long timeSpan, ChronoUnit unit) {
        this.timeSpan = timeSpan;
        this.unit = unit;
        this.windowTime = unit.getDuration().toMillis() * timeSpan;
    }

    /**
     * Calculates the moving average of the time series using the following algorithm:
     * <p>
     * 1) Get all points within the defined time window
     * -> Calculate the time and value average sum(values)/#points
     * <p>
     * 2) If the distance of two timestamps (i, j) is larger than the time window
     * -> Ignore the emtpy window
     * -> Use the j as start of the window and continue with step 1)
     *
     * @param timeSeries the time series that is transformed
     * @return the transformed time series
     */
    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {

        //we need a sorted time series
        timeSeries.sort();

        //get the raw values as arrays
        double[] values = timeSeries.getValuesAsArray();
        long[] times = timeSeries.getTimestampsAsArray();

        //the
        LongList timesMovAvg = new LongList(times.length);
        DoubleList valuesMovAvg = new DoubleList(values.length);

        long start = times[0];
        int startIdx = 0;

        int timeSeriesSize = timeSeries.size();
        //remove the old values
        timeSeries.clear();

        //the start is already set
        for (int i = 1; i < timeSeriesSize; i++) {
            long current = times[i];
            long currentWindowEnd = start + windowTime;

            //If we are inside the window
            if (outsideWindow(currentWindowEnd, current)) {
                startIdx = i;
                start = times[startIdx];
            }

            //calculate the average of the values and the time
            double movAvg = calcAvg(values, startIdx, i);
            long timeAvg = calcAvg(times, startIdx, i);

            timesMovAvg.add(timeAvg);
            valuesMovAvg.add(movAvg);
        }

        //add them to clean time series
        timeSeries.addAll(timesMovAvg.toArray(), valuesMovAvg.toArray());

        return timeSeries;
    }

    private boolean outsideWindow(long currentWindow, long windowTime) {
        return currentWindow < windowTime;
    }

    /**
     * Calculates the average of the given values
     *
     * @param values   the values for the time range
     * @param startIdx the start of the values
     * @param end      end of the values
     * @return the average of the values
     */
    private double calcAvg(double[] values, int startIdx, int end) {
        double sum = 0;

        //If the indices are equals, just return the value at the index position
        if (startIdx == end) {
            return values[startIdx];
        }

        for (int i = startIdx; i <= end; i++) {
            double value = values[i];
            sum += value;
        }
        // +1 as we run to <= end
        return sum / (end - startIdx + 1);
    }

    /**
     * Calculates the average of the given timestamps
     *
     * @param times the timestamps of the time range
     * @return the average of the values
     */
    private long calcAvg(long[] times, int startIdx, int end) {
        long sum = 0;

        //If the indices are equals, just return the value at the index position
        if (startIdx == end) {
            return times[startIdx];
        }

        for (int i = startIdx; i <= end; i++) {
            long value = times[i];
            sum += value;
        }
        // +1 as we run to <= end
        return sum / (end - startIdx + 1);
    }

    @Override
    public FunctionType getType() {
        return FunctionType.MOVAVG;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"timeSpan=" + timeSpan, "unit=" + unit.name()};
    }




    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timeSpan", timeSpan)
                .append("unit", unit)
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
        MovingAverage rhs = (MovingAverage) obj;
        return new EqualsBuilder()
                .append(this.timeSpan, rhs.timeSpan)
                .append(this.unit, rhs.unit)
                .append(this.windowTime, rhs.windowTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(timeSpan)
                .append(unit)
                .append(windowTime)
                .toHashCode();
    }
}
