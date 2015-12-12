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

import de.qaware.chronix.solr.query.analysis.collectors.math.LinearRegression;
import de.qaware.chronix.solr.query.analysis.collectors.math.Percentile;
import de.qaware.chronix.solr.query.analysis.collectors.math.StdDev;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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
     * @param timestamps           - the timestamps
     * @param values               - a the values
     * @param analysis             - the analysis (avg, min, max, dev, p, trend, outlier, frequency)
     * @param aggregationArguments - the isAggregation value used for p (0 - 1), e.g., 0.25
     * @return the aggregated value or in case of a high level analysis 1 for anomaly detected or -1 for not.
     */
    public static double evaluate(Stream<Long> timestamps, Stream<Double> values, AnalysisType analysis, String[] aggregationArguments) {
        if (AnalysisType.isHighLevel(analysis)) {
            return highLevelAnalysisEvaluation(timestamps, values, analysis, aggregationArguments);
        } else {
            return aggregationAnalysisEvaluation(values, analysis, aggregationArguments);
        }
    }

    private static double aggregationAnalysisEvaluation(Stream<Double> values, AnalysisType analysis, String[] aggregationArguments) {
        double value;
        switch (analysis) {
            case AVG:
                value = avg(values.iterator());
                break;
            case MIN:
                value = min(values.iterator());
                break;
            case MAX:
                value = max(values.iterator());
                break;
            case DEV:
                value = StdDev.dev(doubleStream(values).boxed().collect(Collectors.toList()));
                break;
            case P:
                value = Percentile.evaluate(values.collect(Collectors.toList()), Double.parseDouble(aggregationArguments[0]));
                break;
            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The high-level analysis " + analysis + " is not present within the enum.");
        }

        return value;
    }

    private static double max(Iterator<Double> values) {
        double current = values.hasNext() ? values.next() : 0;

        while (values.hasNext()) {
            double next = values.next();

            if (current < next) {
                current = next;
            }
        }
        return current;
    }

    private static double min(Iterator<Double> values) {
        double current = values.hasNext() ? values.next() : 0;

        while (values.hasNext()) {
            double next = values.next();

            if (current > next) {
                current = next;
            }
        }
        return current;
    }

    private static double avg(Iterator<Double> values) {
        double current = 0;
        double count = 0;
        while (values.hasNext()) {
            current += values.next();
            count++;
        }
        return current / count;
    }

    private static double highLevelAnalysisEvaluation(Stream<Long> timestamps, Stream<Double> values, AnalysisType analysis, String[] aggregationArguments) {
        switch (analysis) {
            case OUTLIER:
                return detectOutlier(values);
            case TREND:
                return detectTrend(timestamps, values);
            case FREQUENCY:
                return detectFrequency(timestamps, aggregationArguments);
            default:
                throw new EnumConstantNotPresentException(AnalysisType.class, "The aggregation " + analysis + " is not present within the enum.");
        }
    }

    private static DoubleStream doubleStream(Stream<Double> points) {
        return points.mapToDouble(p -> p);
    }

    private static int detectFrequency(Stream<Long> timestamps, String[] analysisArguments) {
        long windowSize = Long.parseLong(analysisArguments[0]);
        long windowThreshold = Long.parseLong(analysisArguments[1]);

        List<Long> currentWindow = new ArrayList<>();
        List<Integer> windowCount = new ArrayList<>();

        long windowStart = -1;
        long windowEnd = -1;

        Iterator<Long> it = timestamps.iterator();

        while (it.hasNext()) {
            long current = it.next();

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

    private static int detectTrend(Stream<Long> timestamps, Stream<Double> values) {

        LinearRegression linearRegression = new LinearRegression(timestamps.collect(Collectors.toList()), values.collect(Collectors.toList()));

        double slope = linearRegression.slope();

        return slope > 0 ? 1 : -1;
    }

    private static int detectOutlier(Stream<Double> points) {
        boolean detected;

        List<Double> collectedPoint = points.collect(Collectors.toList());

        double q1 = Percentile.evaluate(collectedPoint, .25);
        double q3 = Percentile.evaluate(collectedPoint, .75);
        double threshold = (q3 - q1) * 1.5 + q3;
        detected = collectedPoint.stream().filter(point -> point >= threshold).count() > 0;
        return detected ? 1 : -1;
    }

}
