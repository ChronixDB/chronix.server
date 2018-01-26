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

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the divide transformation
 * @author f.lautenschlager
 */
class DivideTest extends Specification {

    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Div","metric")
        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def divide = new Divide()
        divide.setArguments(["2"] as String[])
        def timeSeries = new ChronixMetricTimeSeries("", timeSeriesBuilder.build())
        def analysisResult = new FunctionCtx(1, 1, 1, 1)

        when:
        divide.execute(timeSeries as List, analysisResult)

        then:
        100.times {
            timeSeries.getRawTimeSeries().getValue(it) == (it + 1) / 2d
        }
    }

    def "test getType"() {
        when:
        def divide = new Divide()
        divide.setArguments(["2"] as String[])
        then:
        divide.getQueryName() == "divide"
    }

    def "test getArguments"() {
        when:
        def divide = new Divide()
        divide.setArguments(["2"] as String[])
        then:
        divide.getArguments()[0] == "value=2.0"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Divide()
        def divide4 = new Divide()
        def divide2 = new Divide()
        function.setArguments(["4"] as String[])
        divide4.setArguments(["4"] as String[])
        divide2.setArguments(["2"] as String[])
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(divide4)
        function.hashCode() == divide4.hashCode()
        function.hashCode() != divide2.hashCode()
    }
}
