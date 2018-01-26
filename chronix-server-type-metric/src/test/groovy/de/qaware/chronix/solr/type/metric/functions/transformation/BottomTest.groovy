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
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the bottom transformation
 * @author f.lautenschlager
 */
class BottomTest extends Specification {
    def "test transform"() {
        given:
        def bottom = new Bottom()
        bottom.setArguments(["4"] as String[])
        def analysisResult = new FunctionCtx(1, 1, 1, 1)

        def timeSeriesBuilder = new MetricTimeSeries.Builder("Bottom","metric")
        timeSeriesBuilder.point(1, 5d)
        timeSeriesBuilder.point(2, 99d)
        timeSeriesBuilder.point(3, 3d)
        timeSeriesBuilder.point(4, 5d)
        timeSeriesBuilder.point(5, 65d)
        timeSeriesBuilder.point(6, 23d)

        def timeSeries = new ChronixMetricTimeSeries("", timeSeriesBuilder.build())
        when:
        bottom.execute(timeSeries as List, analysisResult)


        then:
        timeSeries.getRawTimeSeries().size() == 4
        timeSeries.getRawTimeSeries().getValue(0) == 3d
        timeSeries.getRawTimeSeries().getValue(1) == 5d
        timeSeries.getRawTimeSeries().getValue(2) == 5d
        timeSeries.getRawTimeSeries().getValue(3) == 23d

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
        def function = new Bottom()
        def bottom4 = new Bottom()
        def bottom2 = new Bottom()
        function.setArguments(["4"] as String[])
        bottom4.setArguments(["4"] as String[])
        bottom2.setArguments(["2"] as String[])
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(bottom4)
        function.hashCode() == bottom4.hashCode()
        function.hashCode() != bottom2.hashCode()
    }

    def "test string representation"() {
        expect:
        def bottom = new Bottom()
        bottom.setArguments(["4"] as String[])
        def string = bottom.toString()
        string.contains("value")
    }
}
