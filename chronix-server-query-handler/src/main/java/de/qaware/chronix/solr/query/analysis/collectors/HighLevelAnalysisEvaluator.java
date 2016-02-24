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
package de.qaware.chronix.solr.query.analysis.collectors;

import de.qaware.chronix.distance.DistanceFunction;
import de.qaware.chronix.distance.DistanceFunctionEnum;
import de.qaware.chronix.distance.DistanceFunctionFactory;
import de.qaware.chronix.dtw.FastDTW;
import de.qaware.chronix.dtw.TimeWarpInfo;
import de.qaware.chronix.solr.query.analysis.collectors.math.LinearRegression;
import de.qaware.chronix.solr.query.analysis.collectors.math.Percentile;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.MultivariateTimeSeries;
import de.qaware.chronix.timeseries.dt.DoubleList;
import de.qaware.chronix.timeseries.dt.LongList;
import de.qaware.chronix.timeseries.dt.Pair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to evaluate the high level analyses
 *
 * @author f.lautenschlager
 */
public final class HighLevelAnalysisEvaluator {

    private HighLevelAnalysisEvaluator() {
        //avoid instances
    }

    /**
     * Applies an high level analysis (outlier, trend, frequency) on the timestamps and values.
     * If the result is =-1, the time series contains suspicions values, otherwise not.
     *
     * @param timeSeries the time series
     * @param analysis   the analysis itself
     * @return -1 if no suspicious values are found, otherwise 1 which means that there are suspicious values.
     */
    public static double analyze(MetricTimeSeries timeSeries, ChronixAnalysis analysis) {
        switch (analysis.getType()) {
            case OUTLIER:
                return detectOutlier(timeSeries.getValues());
            case TREND:
                return detectTrend(timeSeries.getTimestamps(), timeSeries.getValues());
            case FREQUENCY:
                return detectFrequency(timeSeries.getTimestamps(), analysis.getArguments());
            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The analysis " + analysis + " is not present within the enum.");
        }
    }

    /**
     * Applies an high level analysis (fastdtw) that need tow time series
     *
     * @param timeSeries    the first time series
     * @param subTimeSeries the second time series
     * @param analysis      the chronix analysis
     * @return -1 if no suspicious values are found, > -1 which means that there are suspicious values.
     */
    public static double analyze(MetricTimeSeries timeSeries, MetricTimeSeries subTimeSeries, ChronixAnalysis analysis) {
        switch (analysis.getType()) {
            case FASTDTW:
                int searchRadius = Integer.parseInt(analysis.getArguments()[1]);
                double maxDifference = Double.parseDouble(analysis.getArguments()[2]);
                DistanceFunction distanceFunction = DistanceFunctionFactory.getDistanceFunction(DistanceFunctionEnum.EUCLIDEAN);
                return fastDTW(timeSeries, subTimeSeries, searchRadius, distanceFunction, maxDifference);

            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The analysis " + analysis + " is not present within the enum.");
        }
    }

    private static double fastDTW(MetricTimeSeries timeSeries, MetricTimeSeries subTimeSeries, int searchRadius, DistanceFunction distanceFunction, double maxDifference) {
        MultivariateTimeSeries origin = buildMultiVariateTimeSeries(timeSeries);
        MultivariateTimeSeries other = buildMultiVariateTimeSeries(subTimeSeries);
        TimeWarpInfo result = FastDTW.getWarpInfoBetween(origin, other, searchRadius, distanceFunction);
        if (result.getNormalizedDistance() <= maxDifference) {
            return result.getNormalizedDistance();
        }
        return -1;
    }

    private static MultivariateTimeSeries buildMultiVariateTimeSeries(MetricTimeSeries timeSeries) {
        MultivariateTimeSeries multivariateTimeSeries = new MultivariateTimeSeries(1);
        timeSeries.sort();
        timeSeries.points().forEachOrdered(pair -> {
            multivariateTimeSeries.add(pair.getTimestamp(), new double[]{pair.getValue()});
        });
        return multivariateTimeSeries;
    }

    /**
     * Detects if a points occurs multiple times within a defined time range
     *
     * @param timestamps        the timestamps of the time series
     * @param analysisArguments the additional analysis arguments
     * @return 1 if there are more points than the defined threshold, otherwise -1
     */
    private static int detectFrequency(LongList timestamps, String[] analysisArguments) {
        long windowSize = Long.parseLong(analysisArguments[0]);
        long windowThreshold = Long.parseLong(analysisArguments[1]);

        List<Long> currentWindow = new ArrayList<>();
        List<Integer> windowCount = new ArrayList<>();

        long windowStart = -1;
        long windowEnd = -1;

        for (int i = 0; i < timestamps.size(); i++) {

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

    /**
     * Detects trends in time series using a linear regression.
     *
     * @param timestamps the timestamps of the time series
     * @param values     the belonging values
     * @return 1 if there is a positive trend, otherwise -1
     */
    private static int detectTrend(LongList timestamps, DoubleList values) {
        LinearRegression linearRegression = new LinearRegression(timestamps, values);
        double slope = linearRegression.slope();
        return slope > 0 ? 1 : -1;
    }

    /**
     * Detects outliers using the default box plot implementation.
     *
     * @param points the values
     * @return 1 if there are outliers, otherwise -1
     */
    private static int detectOutlier(DoubleList points) {

        double q1 = Percentile.evaluate(points, .25);
        double q3 = Percentile.evaluate(points, .75);
        double threshold = (q3 - q1) * 1.5 + q3;
        for (int i = 0; i < points.size(); i++) {
            double point = points.get(i);
            if (point >= threshold) {
                return 1;
            }
        }

        return -1;
    }


}
