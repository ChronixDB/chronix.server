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

import java.sql.Time

/**
 * Unit test for the timeshift transformation
 * @author f.lautenschlager
 */
class TimeshiftTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Timeshift","metric")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = new ChronixMetricTimeSeries("", timeSeriesBuilder.build())
        def analysisResult = new FunctionCtx(1, 1, 1)


        def timeshift = new Timeshift()
        timeshift.setArguments(["4", "MILLIS"] as String[])

        when:
        timeshift.execute(timeSeries as List, analysisResult)
        then:
        timeSeries.getRawTimeSeries().size() == 11
        timeSeries.getRawTimeSeries().getTime(0) == 4
        timeSeries.getRawTimeSeries().getValue(0) == 10

        timeSeries.getRawTimeSeries().getTime(10) == 1004
        timeSeries.getRawTimeSeries().getValue(10) == -10
    }

    def "test transform for negative amount"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Timeshift","metric")
        10.times {
            timeSeriesBuilder.point(it * 100, it + 10)
        }
        timeSeriesBuilder.point(10 * 100, -10)
        def timeSeries = new ChronixMetricTimeSeries("", timeSeriesBuilder.build())
        def analysisResult = new FunctionCtx(1, 1, 1)

        def timeshift = new Timeshift()
        timeshift.setArguments(["-4", "MILLIS"] as String[])

        when:
        timeshift.execute(timeSeries as List, analysisResult)
        then:
        timeSeries.getRawTimeSeries().size() == 11
        timeSeries.getRawTimeSeries().getTime(0) == -4
        timeSeries.getRawTimeSeries().getValue(0) == 10

        timeSeries.getRawTimeSeries().getTime(10) == 996
        timeSeries.getRawTimeSeries().getValue(10) == -10
    }

    def "test getType"() {
        expect:
        def timeshift = new Timeshift()
        timeshift.setArguments(["4", "DAYS"] as String[])
        timeshift.getQueryName() == "timeshift"
    }

    def "test getArguments"() {
        expect:
        def timeshift = new Timeshift()
        timeshift.setArguments(["4", "DAYS"] as String[])
        timeshift.getArguments()[0] == "amount=4"
        timeshift.getArguments()[1] == "unit=DAYS"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Timeshift()
        def timeshift4 = new Timeshift()
        def timeshift2 = new Timeshift()
        def timeshift4sec = new Timeshift()
        function.setArguments(["4", "DAYS"] as String[])
        timeshift4.setArguments(["4", "DAYS"] as String[])
        timeshift2.setArguments(["4", "DAYS"] as String[])
        timeshift4sec.setArguments(["4", "DAYS"] as String[])
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(timeshift4)
        function.hashCode() == timeshift4.hashCode()
        function.hashCode() != timeshift2.hashCode()
        function.hashCode() != timeshift4sec.hashCode()
    }

    def "test string representation"() {
        expect:
        def timeshift = new Timeshift()
        timeshift.setArguments(["4", "DAYS"] as String[])
        def string = timeshift.toString()
        string.contains("unit")
        string.contains("amount")
    }
}
