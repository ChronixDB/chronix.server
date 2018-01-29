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
 * Unit test for the add transformation
 * @author f.lautenschlager
 */
class AddTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Add", "metric")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = new ChronixMetricTimeSeries("", timeSeriesBuilder.build())
        def analysisResult = new FunctionCtx(1, 1, 1)

        def add = new Add()
        add.setArguments(["4"] as String[])

        when:
        add.execute([timeSeries] as List, analysisResult)
        then:
        timeSeries.getRawTimeSeries().size() == 11
        timeSeries.getRawTimeSeries().getValue(1) == 1 + 10 + 4

        timeSeries.getRawTimeSeries().getValue(10) == -6
    }

    def "test getType"() {
        expect:
        new Add().getQueryName() == "add"
    }

    def "test getArguments"() {
        expect:
        def add = new Add()
        add.setArguments(["4"] as String[])
        add.getArguments()[0] == "value=4.0"
    }

    def "test equals and hash code"() {
        given:
        def function = new Add()
        function.setArguments(["4"] as String[])

        def sameFunction = new Add()
        sameFunction.setArguments(["4"] as String[])

        def otherFunction = new Add()
        otherFunction.setArguments(["2"] as String[])

        expect:
        function != null
        function != new Object()
        function == function
        function == sameFunction
        function.hashCode() == sameFunction.hashCode()
        function.hashCode() != otherFunction.hashCode()
    }

    def "test string representation"() {
        expect:
        new Add().toString() contains("value")
    }
}
