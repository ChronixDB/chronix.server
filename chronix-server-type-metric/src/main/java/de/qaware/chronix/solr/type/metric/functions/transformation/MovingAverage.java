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
package de.qaware.chronix.solr.type.metric.functions.transformation;

import de.qaware.chronix.server.functions.ChronixTransformation;
import de.qaware.chronix.server.functions.FunctionCtx;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.temporal.ChronoUnit;

/**
 * The moving average transformation.
 *
 * @author f.lautenschlager
 */
public final class MovingAverage implements ChronixTransformation<MetricTimeSeries> {

    private final long timeSpan;
    private final ChronoUnit unit;
    private final long windowTime;

    /**
     * Constructs a moving average transformation
     *
     * @param args the first value is the time span e.g. 5, 10, the second one is the unit of the time span
     */
    public MovingAverage(String[] args) {

        this.timeSpan = Long.parseLong(args[0]);
        this.unit = ChronoUnit.valueOf(args[1].toUpperCase());
        this.windowTime = unit.getDuration().toMillis() * timeSpan;
    }

    /**
     * Calculates the moving average by sliding a window (timeSpan * unit, set in the constructor) over the time series.
     * First the window is filled with points whose timestamps are within the window and the averages of the window
     * are evaluated. Then the window slides over the next point, calculates the end and evaluates the averages and so on.
     * If the next point is not within the window, it slides anyway. The first point is dropped and the averages
     * of the remaining points are evaluated. If the next point also not within the window, again the first point is
     * dropped and so on until the end of the window is greater equals the time series end.
     * We do this as time series can have gaps that are larger than the defined window.
     *
     * @param timeSeries the time series that is transformed
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionCtx functionCtx) {

        //we need a sorted time series
        timeSeries.sort();

        //get the raw values as arrays
        double[] values = timeSeries.getValuesAsArray();
        long[] times = timeSeries.getTimestampsAsArray();

        int timeSeriesSize = timeSeries.size();
        //remove the old values
        timeSeries.clear();

        int startIdx = 0;
        long current = times[0];
        long currentWindowEnd = current + windowTime;
        long last = times[timeSeriesSize - 1];

        boolean lastWindowOnlyOnePoint = true;

        //the start is already set
        for (int i = 0; i < timeSeriesSize; i++) {

            //fill window
            while (i < timeSeriesSize && !outsideWindow(currentWindowEnd, current)) {
                current = times[i++];
            }
            //decrement counter to mark the last index position that is within the window
            i -= 1;

            //calculate the average of the values and the time
            evaluateAveragesAndAddToTimeSeries(timeSeries, values, times, startIdx, i);

            //slide the window
            startIdx++;
            currentWindowEnd = times[startIdx] + windowTime;

            //check if the current window end is larger equals the end timestamp
            if (currentWindowEnd >= last) {
                //break and add the last window
                lastWindowOnlyOnePoint = false;
                break;
            }
        }

        if (lastWindowOnlyOnePoint) {
            timeSeries.add(times[timeSeriesSize - 1], values[timeSeriesSize - 1]);
        } else {
            //add the last window
            evaluateAveragesAndAddToTimeSeries(timeSeries, values, times, startIdx, timeSeriesSize);
        }

        functionCtx.add(this);
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
    private void evaluateAveragesAndAddToTimeSeries(MetricTimeSeries timeSeries, double[] values, long[] times, int startIdx, int end) {

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

    private boolean outsideWindow(long currentWindow, long windowTime) {
        return currentWindow < windowTime;
    }

    @Override
    public String getQueryName() {
        return "movavg";
    }

    @Override
    public String getType() {
        return "metric";
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
