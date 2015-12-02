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
package de.qaware.chronix.solr.query.analysis.aggregation.aggregator;

import de.qaware.chronix.solr.query.analysis.aggregation.aggregator.math.Percentile;
import de.qaware.chronix.solr.query.analysis.aggregation.aggregator.math.StdDev;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Aggregation evaluator supports AVG, MIN, max, dev and percentile
 *
 * @author f.lautenschlager
 */
public class AggregationEvaluator {


    private AggregationEvaluator() {
        //avoid instances
    }

    /**
     * Evaluates the given aggregation on the given time series
     *
     * @param points           - a stream of doubles
     * @param aggregation      - the aggregation (AVG, MIN, max, dev, p)
     * @param aggregationValue - the aggregation value used for p (0 - 1), e.g., 0.25
     * @return
     */
    public static double evaluate(DoubleStream points, AggregationType aggregation, double aggregationValue) {
        double value;

        //now lets aggregate them
        switch (aggregation) {
            case AVG:
                value = points.average().getAsDouble();
                break;
            case MIN:
                value = points.min().getAsDouble();
                break;
            case MAX:
                value = points.max().getAsDouble();
                break;
            case DEV:
                value = StdDev.dev(points.boxed().collect(Collectors.toList()));
                break;
            case P:
                value = Percentile.evaluate(points, aggregationValue);
                break;
            default:
                throw new EnumConstantNotPresentException(AggregationType.class, "The value " + aggregation + " is not present within the enum.");
        }

        return value;
    }

   
}
