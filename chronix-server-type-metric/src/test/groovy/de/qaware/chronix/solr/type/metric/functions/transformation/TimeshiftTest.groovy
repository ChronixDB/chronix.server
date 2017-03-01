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

/**
 * Unit test for the timeshift transformation
 * @author f.lautenschlager
 */
class TimeshiftTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Timeshift")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);


        def timeshift = new Timeshift(["4", "MILLIS"] as String[])

        when:
        timeshift.execute(timeSeries, analysisResult)
        then:
        timeSeries.size() == 11
        timeSeries.getTime(0) == 4
        timeSeries.getValue(0) == 10

        timeSeries.getTime(10) == 1004
        timeSeries.getValue(10) == -10
    }

    def "test transform for negative amount"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Timeshift")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        def timeshift = new Timeshift(["-4", "MILLIS"] as String[])

        when:
        timeshift.execute(timeSeries, analysisResult)
        then:
        timeSeries.size() == 11
        timeSeries.getTime(0) == -4
        timeSeries.getValue(0) == 10

        timeSeries.getTime(10) == 996
        timeSeries.getValue(10) == -10
    }

    def "test getType"() {
        expect:
        new Timeshift(["4", "DAYS"] as String[]).getQueryName() == "timeshift"
    }

    def "test getArguments"() {
        expect:
        new Timeshift(["4", "DAYS"] as String[]).getArguments()[0] == "amount=4"
        new Timeshift(["4", "DAYS"] as String[]).getArguments()[1] == "unit=DAYS"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Timeshift(["4", "DAYS"] as String[]);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Timeshift(["4", "DAYS"] as String[]))
        new Timeshift(["4", "DAYS"] as String[]).hashCode() == new Timeshift(["4", "DAYS"] as String[]).hashCode()
        new Timeshift(["4", "DAYS"] as String[]).hashCode() != new Timeshift(["2", "DAYS"] as String[]).hashCode()
        new Timeshift(["4", "DAYS"] as String[]).hashCode() != new Timeshift(["4", "SECONDS"] as String[]).hashCode()
    }

    def "test string representation"() {
        expect:
        def string = new Timeshift(["4", "DAYS"] as String[]).toString()
        string.contains("unit")
        string.contains("amount")
    }
}
