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
 * Unit test for the last function
 * @author f.lautenschlager
 */
class FirstTest extends Specification {

    def "test get last value"() {

        given:
        def timeSeries = new MetricTimeSeries.Builder("Last-Time-Series","metric")
        def analysisResult = new FunctionCtx(1, 1, 1)

        10.times {
            timeSeries.point(10 - it, it)
        }

        when:
        new First().execute(new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeries.build()))), analysisResult)

        then:
        analysisResult.getContextFor("").getAggregationValue(0) == 9d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionCtx(1, 1, 1)

        when:
        new First().execute(new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", new MetricTimeSeries.Builder("Empty","metric").build()))), analysisResult)
        then:
        analysisResult.getContextFor("").getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new First().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new First().getQueryName() == "first"
    }

    def "test equals and hash code"() {
        expect:
        def first = new First();
        !first.equals(null)
        !first.equals(new Object())
        first.equals(first)
        first.equals(new First())
        new First().hashCode() == new First().hashCode()
    }
}
