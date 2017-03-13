/*
 * Copyright (C) 2016 QAware GmbH
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

import de.qaware.chronix.server.functions.FunctionValueMap
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

        def divide = new Divide(["2"] as String[])
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1)

        when:
        divide.execute(timeSeries, analysisResult)

        then:
        100.times {
            timeSeries.getValue(it) == (it + 1) / 2d
        }
    }

    def "test getType"() {
        when:
        def divide = new Divide(["2"] as String[])
        then:
        divide.getQueryName() == "divide"
    }

    def "test getArguments"() {
        when:
        def divide = new Divide(["2"] as String[])
        then:
        divide.getArguments()[0] == "value=2.0"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Divide(["4"] as String[])
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Divide(["4"] as String[]))
        new Divide(["4"] as String[]).hashCode() == new Divide(["4"] as String[]).hashCode()
        new Divide(["4"] as String[]).hashCode() != new Divide(["2"] as String[]).hashCode()
    }
}
