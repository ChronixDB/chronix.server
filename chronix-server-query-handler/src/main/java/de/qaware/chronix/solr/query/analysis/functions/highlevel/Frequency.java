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
package de.qaware.chronix.solr.query.analysis.functions.highlevel;

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType;
import de.qaware.chronix.solr.query.analysis.functions.ChronixAnalysis;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.LongList;
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
public final class Frequency implements ChronixAnalysis {

    private final long windowSize;
    private final long windowThreshold;

    /**
     * Constructs a frequency detection
     *
     * @param windowSize      the window size in minutes
     * @param windowThreshold the threshold per window
     */
    public Frequency(long windowSize, long windowThreshold) {
        this.windowSize = windowSize;
        this.windowThreshold = windowThreshold;
    }

    /**
     * Detects if a points occurs multiple times within a defined time range
     *
     * @param args the time series
     * @return 1 if there are more points than the defined threshold, otherwise -1
     */
    @Override
    public double execute(MetricTimeSeries... args) {
        if (args.length <= 0) {
            throw new IllegalArgumentException("Frequency needs at least one time series");
        }
        MetricTimeSeries timeSeries = args[0];
        LongList timestamps = timeSeries.getTimestamps();

        final List<Long> currentWindow = new ArrayList<>();
        final List<Integer> windowCount = new ArrayList<>();

        //start and end of the window
        long windowStart = -1;
        long windowEnd = -1;

        for (int i = 0; i < timeSeries.size(); i++) {

            long current = timestamps.get(i);

            //The start is marked with -1
            if (windowStart == -1) {
                //Set it to the start
                windowStart = current;
                windowEnd = Instant.ofEpochMilli(windowStart).plus(windowSize, ChronoUnit.MINUTES).toEpochMilli();
            }
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
                return 1;
            }
        }
        //Nothing bad found
        return -1;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"window size=" + windowSize, "window threshold=" + windowThreshold};
    }

    @Override
    public AnalysisType getType() {
        return AnalysisType.FREQUENCY;
    }

    @Override
    public boolean needSubquery() {
        return false;
    }

    @Override
    public String getSubquery() {
        return null;
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
