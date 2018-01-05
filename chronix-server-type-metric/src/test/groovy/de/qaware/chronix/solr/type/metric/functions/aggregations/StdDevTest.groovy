/*
 * Copyright (C) 2018 QAware GmbH
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
package de.qaware.chronix.solr.type.metric.functions.aggregations

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.server.types.ChronixTimeSeries
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the standard deviation aggregation
 * @author f.lautenschlager
 */
class StdDevTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Stddev","metric")
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionCtx(1, 1, 1)

        when:
        new StdDev().execute(new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", ts))), analysisResult)
        then:
        analysisResult.getContextFor("").getAggregationValue(0) == 3001.381363790528d
    }


    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionCtx(1, 1, 1)
        when:
        new StdDev().execute(new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", new MetricTimeSeries.Builder("Empty","metric").build()))), analysisResult)
        then:
        analysisResult.getContextFor("").getAggregationValue(0) == Double.NaN
    }

    def "test arguments"() {
        expect:
        new StdDev().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new StdDev().getQueryName() == "dev"
    }

    def "test equals and hash code"() {
        expect:
        def stdDev = new StdDev()
        !stdDev.equals(null)
        !stdDev.equals(new Object())
        stdDev.equals(stdDev)
        stdDev.equals(new StdDev())
        new StdDev().hashCode() == new StdDev().hashCode()
    }

}
