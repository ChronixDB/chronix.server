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
package de.qaware.chronix.solr.type.metric.functions.analyses;

import de.qaware.chronix.converter.common.LongList;
import de.qaware.chronix.server.functions.ChronixAnalysis;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * The frequency detection analysis.
 * Checks if the occurrence within a defined window (in minutes) is above a defined threshold.
 *
 * @author f.lautenschlager
 */
public final class Frequency implements ChronixAnalysis<MetricTimeSeries> {

    private final long windowSize;
    private final long windowThreshold;

    /**
     * Constructs a frequency detection
     *
     * @param args The first arguemnt is the windowSize, the second the window threshold
     */
    public Frequency(String[] args) {

        this.windowSize = Long.parseLong(args[0]);
        this.windowThreshold = Long.parseLong(args[1]);
    }

    /**
     * The frequency detector splits a time series into windows, counts the data points, and checks if the delta
     * between two windows is above a predefined threshold.
     * <p>
     * The frequency detector splits a time series using the constructor argument.
     *
     * @param functionValueMap
     * @return true if the time series has a pair of windows 1 and 2 where 2 has th
     */
    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        LongList timestamps = timeSeries.getTimestamps();

        final List<Long> currentWindow = new ArrayList<>();
        final List<Integer> windowCount = new ArrayList<>();

        //start and end of the window
        long windowStart = timestamps.get(0);
        //calculate the end
        long windowEnd = Instant.ofEpochMilli(windowStart).plus(windowSize, ChronoUnit.MINUTES).toEpochMilli();

        for (int i = 1; i < timeSeries.size(); i++) {
            long current = timestamps.get(i);
            //Add the occurrence of the current window.
            if (current > windowStart - 1 && current < (windowEnd)) {
                currentWindow.add(current);
            } else {
                //We reached the end. Lets add it to the window count
                windowCount.add(currentWindow.size());
                windowStart = current;
                windowEnd = Instant.ofEpochMilli(windowStart).plus(windowSize, ChronoUnit.MINUTES).toEpochMilli();
                currentWindow.clear();
            }
        }
        //we are done, add the last window
        windowCount.add(currentWindow.size());

        //check deltas
        for (int i = 1; i < windowCount.size(); i++) {

            int former = windowCount.get(i - 1);
            int current = windowCount.get(i);

            //The threshold
            int result = current - former;
            if (result >= windowThreshold) {
                //add the time series as there are more points per window than the threshold
                functionValueMap.add(this, true, null);
                return;
            }
        }
        //Nothing bad found
        functionValueMap.add(this, false, null);

    }

    @Override
    public String[] getArguments() {
        return new String[]{"window size=" + windowSize, "window threshold=" + windowThreshold};
    }

    @Override
    public String getQueryName() {
        return "frequency";
    }

    @Override
    public String getTimeSeriesType() {
        return "metric";
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("windowSize", windowSize)
                .append("windowThreshold", windowThreshold)
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
        Frequency rhs = (Frequency) obj;
        return new EqualsBuilder()
                .append(this.windowSize, rhs.windowSize)
                .append(this.windowThreshold, rhs.windowThreshold)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(windowSize)
                .append(windowThreshold)
                .toHashCode();
    }
}
