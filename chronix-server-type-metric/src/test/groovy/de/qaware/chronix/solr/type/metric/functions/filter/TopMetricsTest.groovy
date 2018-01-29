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
package de.qaware.chronix.solr.type.metric.functions.filter

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.server.types.ChronixTimeSeries
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the topmetrics filter
 * @author k.just
 */
class TopMetricsTest extends Specification {
    def "test filter"() {
        given:
        def topMetrics = new TopMetrics()
        topMetrics.setArguments(["1"] as String[])

        def timeSeriesBuilderLow = new MetricTimeSeries.Builder("TopMetricsLow","metric")
        def timeSeriesBuilderHigh = new MetricTimeSeries.Builder("TopMetricsHigh", "metric")

        timeSeriesBuilderLow.point(1, 5d)
        timeSeriesBuilderLow.point(2, 99d)
        timeSeriesBuilderLow.point(3, 3d)
        timeSeriesBuilderLow.point(4, 5d)
        timeSeriesBuilderLow.point(5, 65d)
        timeSeriesBuilderLow.point(6, 23d)

        timeSeriesBuilderHigh.point(1, 10d)
        timeSeriesBuilderHigh.point(2, 188d)
        timeSeriesBuilderHigh.point(3, 6d)
        timeSeriesBuilderHigh.point(4, 10d)
        timeSeriesBuilderHigh.point(5, 130d)
        timeSeriesBuilderHigh.point(6, 46d)

        def timeSeriesLow = new ChronixMetricTimeSeries("Low", timeSeriesBuilderLow.build())
        def timeSeriesHigh = new ChronixMetricTimeSeries("High", timeSeriesBuilderHigh.build())

        def timeSeriesList = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>()
        timeSeriesList.add(timeSeriesLow)
        timeSeriesList.add(timeSeriesHigh)

        def analysisResult = new FunctionCtx(1, 1, 1, 1)

        when:
        topMetrics.execute(timeSeriesList, analysisResult)


        then:
        timeSeriesList.size() == 1
        timeSeriesList.get(0).getJoinKey() == "High"

    }

    def "test getType"() {
        when:
        def topMetrics = new TopMetrics()
        then:
        topMetrics.getQueryName() == "topmetrics"
    }

    def "test getArguments"() {
        when:
        def topMetrics = new TopMetrics()
        topMetrics.setArguments(["2"] as String[])
        then:
        topMetrics.getArguments()[0] == "value=2"
    }

    def "test equals and hash code"() {
        expect:
        def function = new TopMetrics()
        def test_func4 = new TopMetrics()
        def test_func2 = new TopMetrics()
        function.setArguments(["4"] as String[])
        test_func4.setArguments(["4"] as String[])
        test_func2.setArguments(["2"] as String[])

        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(test_func4)
        function.hashCode() == test_func4.hashCode()
        function.hashCode() != test_func2.hashCode()
    }

    def "test string representation"() {
        expect:
        def topMetrics = new TopMetrics()
        topMetrics.setArguments(["4"] as String[])
        def string = topMetrics.toString()
        string.contains("value")
    }
}
