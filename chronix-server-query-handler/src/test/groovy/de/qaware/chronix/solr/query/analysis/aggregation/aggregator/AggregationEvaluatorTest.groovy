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
package de.qaware.chronix.solr.query.analysis.aggregation.aggregator
import de.qaware.chronix.dts.MetricDataPoint
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.ToDoubleFunction
import java.util.stream.DoubleStream
/**
 * Unit test for the aggregation Evaluator
 * @author f.lautenschlager
 */
class AggregationEvaluatorTest extends Specification {
    @Unroll
    def "test evaluate aggregation '#aggregation'. Expected value is #expected"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Aggregation");
        10.times {
            timeSeries.point(new MetricDataPoint(it, it * 10))
        }
        MetricTimeSeries ts = timeSeries.build()

        DoubleStream points = ts.getPoints().stream().mapToDouble(new ToDoubleFunction<MetricDataPoint>() {
            @Override
            double applyAsDouble(MetricDataPoint value) {
                return value.getValue();
            }
        })

        when:
        double aggregationValue = AggregationEvaluator.evaluate(points, aggregation, 0.25d)

        then:
        aggregationValue == expected

        where:
        aggregation << [AggregationType.MIN, AggregationType.MAX, AggregationType.AVG, AggregationType.DEV, AggregationType.P]
        expected << [0d, 90.0d, 45, 30.276503540974915, 22.5]
    }

    def "test private constructor"() {
        when:
        AggregationEvaluator.newInstance()

        then:
        noExceptionThrown()
    }
}
