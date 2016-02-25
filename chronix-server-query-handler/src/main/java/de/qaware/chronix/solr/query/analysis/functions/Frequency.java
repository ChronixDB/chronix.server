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
package de.qaware.chronix.solr.query.analysis.functions;

import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.LongList;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * The frequency detection analysis
 *
 * @author f.lautenschlager
 */
public final class Frequency implements ChronixAnalysis {

    private final long windowSize;
    private final long windowThreshold;

    /**
     * Constructs a frequency detection
     *
     * @param windowSize      the window size
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
            throw new IllegalArgumentException("Fast DTW needs at least one time series");
        }
        MetricTimeSeries timeSeries = args[0];
        LongList timestamps = timeSeries.getTimestamps();

        final List<Long> currentWindow = new ArrayList<>();
        final List<Integer> windowCount = new ArrayList<>();

        long windowStart = -1;
        long windowEnd = -1;

        for (int i = 0; i < timeSeries.size(); i++) {

            long current = timestamps.get(i);

            if (windowStart == -1) {
                windowStart = current;
                windowEnd = Instant.ofEpochMilli(windowStart).plus(windowSize, ChronoUnit.MINUTES).toEpochMilli();
            }

            if (current > windowStart - 1 && current < (windowEnd)) {
                currentWindow.add(current);
            } else {
                windowCount.add(currentWindow.size());
                windowStart = current;
                windowEnd = Instant.ofEpochMilli(windowStart).plus(windowSize, ChronoUnit.MINUTES).toEpochMilli();
                currentWindow.clear();
            }
        }

        //check deltas
        for (int i = 1; i < windowCount.size(); i++) {

            int former = windowCount.get(i - 1);
            int current = windowCount.get(i);

            int result = current - former;
            if (result >= windowThreshold) {
                //add the time series as anomalous
                return 1;
            }
        }
        return -1;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{windowSize, windowThreshold};
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
}
