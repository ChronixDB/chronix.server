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

/**
 * Unit test for the sample moving average transformation
 * @author f.lautenschlager
 */
class SampleMovingAverageTest extends Specification {

    def "test transform with last window contains only one point"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Sample Moving average","metric")
        def movAvg = new SampleMovingAverage(["5"] as String[])

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:00.000Z"), 5)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:01.000Z"), 4)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:02.000Z"), 3)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:03.000Z"), 8)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:04.000Z"), 4)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:05.000Z"), 6)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:06.000Z"), 10)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:07.000Z"), 31)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:08.000Z"), 9)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:09.000Z"), 11)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:10.000Z"), 12)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:10.500Z"), 13)


        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1)

        when:
        movAvg.execute(timeSeries, analysisResult)
        then:
        timeSeries.size() == 8
        timeSeries.getValue(0) == 4.8d
        timeSeries.getTime(0) == dateOf("2016-05-23T10:51:02.000Z")
        timeSeries.getValue(1) == 5.0d
        timeSeries.getTime(1) == dateOf("2016-05-23T10:51:03.000Z")
        timeSeries.getValue(2) == 6.2d
        timeSeries.getTime(2) == dateOf("2016-05-23T10:51:04.000Z")
        timeSeries.getValue(3) == 11.8d
        timeSeries.getTime(3) == dateOf("2016-05-23T10:51:05.000Z")
        timeSeries.getValue(4) == 12.0d
        timeSeries.getTime(4) == dateOf("2016-05-23T10:51:06.000Z")
        timeSeries.getValue(5) == 13.4d
        timeSeries.getTime(5) == dateOf("2016-05-23T10:51:07.000Z")
        timeSeries.getValue(6) == 14.6d
        timeSeries.getTime(6) == dateOf("2016-05-23T10:51:08.000Z")
        timeSeries.getValue(7) == 15.2d
        timeSeries.getTime(7) == dateOf("2016-05-23T10:51:08.900Z")
    }


    def long dateOf(def format) {
        Instant.parse(format as String).toEpochMilli()
    }

    def "test getType"() {
        when:
        def movAvg = new SampleMovingAverage(["4"] as String[])

        then:
        movAvg.getQueryName() == "smovavg"
    }

    def "test getArguments"() {
        when:
        def movAvg = new SampleMovingAverage(["4"] as String[])
        then:
        movAvg.getArguments()[0] == "samples=4"
    }

    def "test toString"() {
        expect:
        def stringRepresentation = new SampleMovingAverage(["4"] as String[]).toString()
        stringRepresentation.contains("samples")
    }

    def "test equals and hash code"() {
        expect:
        def function = new SampleMovingAverage(["5"] as String[])
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new SampleMovingAverage(["5"] as String[]))
        new SampleMovingAverage(["4"] as String[]).hashCode() == new SampleMovingAverage(["4"] as String[]).hashCode()
        new SampleMovingAverage(["4"] as String[]).hashCode() != new SampleMovingAverage(["2"] as String[]).hashCode()
    }
}
