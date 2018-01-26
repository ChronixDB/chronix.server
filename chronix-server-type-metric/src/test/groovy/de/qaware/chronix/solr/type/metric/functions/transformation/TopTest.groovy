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
package de.qaware.chronix.solr.type.metric.functions.transformation

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.server.types.ChronixTimeSeries
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the bottom transformation
 * @author f.lautenschlager
 */
class TopTest extends Specification {
    def "test transform"() {
        given:
        def top = new Top()
        top.setArguments(["4"] as String[])

        def timeSeriesBuilder = new MetricTimeSeries.Builder("Top","metric")
        timeSeriesBuilder.point(1, 5d)
        timeSeriesBuilder.point(2, 99d)
        timeSeriesBuilder.point(3, 3d)
        timeSeriesBuilder.point(4, 5d)
        timeSeriesBuilder.point(5, 65d)
        timeSeriesBuilder.point(6, 23d)

        def timeSeries = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeriesBuilder.build())))
        def analysisResult = new FunctionCtx(1, 1, 1, 1)

        when:
        top.execute(timeSeries, analysisResult)


        then:
        timeSeries.get(0).getRawTimeSeries().size() == 4
        timeSeries.get(0).getRawTimeSeries().getValue(0) == 99d
        timeSeries.get(0).getRawTimeSeries().getValue(1) == 65d
        timeSeries.get(0).getRawTimeSeries().getValue(2) == 23d
        timeSeries.get(0).getRawTimeSeries().getValue(3) == 5d

    }

    def "test getType"() {
        when:
        def bottom = new Bottom()
        bottom.setArguments(["2"] as String[])
        then:
        bottom.getQueryName() == "bottom"
    }

    def "test getArguments"() {
        when:
        def bottom = new Bottom()
        bottom.setArguments(["2"] as String[])
        then:
        bottom.getArguments()[0] == "value=2"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Top()
        def test_func4 = new Top()
        def test_func2 = new Top()
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
        def top = new Top()
        top.setArguments(["4"] as String[])
        def string = top.toString()
        string.contains("value")
    }
}
