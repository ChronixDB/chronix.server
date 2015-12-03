/*
 * Copyright (C) 2015 QAware GmbH
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

import de.qaware.chronix.dts.MetricDataPoint;
import de.qaware.chronix.solr.query.analysis.collectors.math.LinearRegression;
import de.qaware.chronix.solr.query.analysis.collectors.math.Percentile;
import de.qaware.chronix.solr.query.analysis.collectors.math.StdDev;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Aggregation evaluator supports AVG, MIN, max, dev and percentile
 *
 * @author f.lautenschlager
 */
public class AnalysisEvaluator {


    private AnalysisEvaluator() {
        //avoid instances
    }

    /**
     * Evaluates the given isAggregation on the given time series
     *
     * @param points               - a stream of doubles
     * @param analysis             - the analysis (avg, min, max, dev, p, trend, outlier, frequency)
     * @param aggregationArguments - the isAggregation value used for p (0 - 1), e.g., 0.25
     * @return the aggregated value or in case of a high level analysis 1 for anomaly detected or -1 for not.
     */
    public static double evaluate(List<MetricDataPoint> points, AnalysisType analysis, String[] aggregationArguments) {
        if (AnalysisType.isHighLevel(analysis)) {
            return highLevelAnalysisEvaluation(points, analysis, aggregationArguments);
        } else {
            return aggregationAnalysisEvaluation(points, analysis, aggregationArguments);
        }
    }

    private static double aggregationAnalysisEvaluation(List<MetricDataPoint> points, AnalysisType analysis, String[] aggregationArguments) {
        double value;
        switch (analysis) {
            case AVG:
                value = doubleStream(points).average().getAsDouble();
                break;
            case MIN:
                value = doubleStream(points).min().getAsDouble();
                break;
            case MAX:
                value = doubleStream(points).max().getAsDouble();
                break;
            case DEV:
                value = StdDev.dev(doubleStream(points).boxed().collect(Collectors.toList()));
                break;
            case P:
                value = Percentile.evaluate(doubleStream(points), Double.parseDouble(aggregationArguments[0]));
                break;
            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The high-level analysis " + analysis + " is not present within the enum.");
        }

        return value;
    }

    private static double highLevelAnalysisEvaluation(List<MetricDataPoint> points, AnalysisType analysis, String[] aggregationArguments) {
        switch (analysis) {
            case OUTLIER:
                return detectOutlier(points);
            case TREND:
                return detectTrend(points);
            case FREQUENCY:
                return detectFrequency(points, aggregationArguments);
            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The aggregation " + analysis + " is not present within the enum.");
        }
    }

    private static DoubleStream doubleStream(List<MetricDataPoint> points) {
        return points.stream().mapToDouble(MetricDataPoint::getValue);
    }

    private static int detectFrequency(List<MetricDataPoint> points, String[] analysisArguments) {
        long windowSize = Long.parseLong(analysisArguments[0]);
        long windowThreshold = Long.parseLong(analysisArguments[1]);

        List<MetricDataPoint> currentWindow = new ArrayList<>();
        List<Integer> windowCount = new ArrayList<>();

        long windowStart = -1;
        long windowEnd = -1;
        for (MetricDataPoint current : points) {

            if (windowStart == -1) {
                windowStart = current.getDate();
                windowEnd = Instant.ofEpochMilli(windowStart).plus(windowSize, ChronoUnit.MINUTES).toEpochMilli();
            }

            if (current.getDate() > windowStart - 1 && current.getDate() < (windowEnd)) {
                currentWindow.add(current);
            } else {
                windowCount.add(currentWindow.size());
                windowStart = current.getDate();
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

    private static int detectTrend(List<MetricDataPoint> points) {

        LinearRegression linearRegression = new LinearRegression(points);

        double slope = linearRegression.slope();

        return slope > 0 ? 1 : -1;
    }

    private static int detectOutlier(List<MetricDataPoint> points) {
        boolean detected;
        double q1 = Percentile.evaluate(points.stream().mapToDouble(MetricDataPoint::getValue), .25);
        double q3 = Percentile.evaluate(points.stream().mapToDouble(MetricDataPoint::getValue), .75);
        double threshold = (q3 - q1) * 1.5 + q3;
        detected = points.stream().filter(point -> point.getValue() >= threshold).count() > 0;
        return detected ? 1 : -1;
    }

}
