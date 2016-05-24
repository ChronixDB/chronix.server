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
package de.qaware.chronix.solr.query.analysis.functions.transformation

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the moving average transformation
 * @author f.lautenschlager
 */
class MovingAverageTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Moving average")
        def movAvg = new MovingAverage(5, ChronoUnit.SECONDS)

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:00.000Z"), 5)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:01.000Z"), 4)

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:06.500Z"), 6)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:07.000Z"), 10)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:08.000Z"), 31)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:09.000Z"), 9)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:10.000Z"), 2)

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:15.000Z"), 1)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:16.000Z"), 5)


        when:
        def movingAvgSeries = movAvg.transform(timeSeriesBuilder.build())
        then:
        movingAvgSeries.size() == 8
        movingAvgSeries.getValue(0) == 4.5d
        movingAvgSeries.getValue(1) == 6.0d
        movingAvgSeries.getValue(2) == 8.0d
        movingAvgSeries.getValue(3) == 15.666666666666666d
        movingAvgSeries.getValue(4) == 14.0d
        movingAvgSeries.getValue(5) == 11.6d
        movingAvgSeries.getValue(6) == 1d
        movingAvgSeries.getValue(7) == 3d


    }

    def long dateOf(def format) {
        Instant.parse(format as String).toEpochMilli()
    }

    def "test getType"() {
        when:
        def movAvg = new MovingAverage(4, ChronoUnit.DAYS)

        then:
        movAvg.getType() == FunctionType.MOVAVG
    }

    def "test getArguments"() {
        when:
        def movAvg = new MovingAverage(4, ChronoUnit.DAYS)

        then:
        movAvg.getArguments()[0] == "timeSpan=4"
        movAvg.getArguments()[1] == "unit=DAYS"
    }

    def "test toString"(){
        expect:
        def stringRepresentation = new MovingAverage(4, ChronoUnit.DAYS).toString();
        stringRepresentation.contains("timeSpan")
        stringRepresentation.contains("unit")
    }

    def "test equals and hash code"() {
        expect:
        def function = new MovingAverage(4,ChronoUnit.DAYS);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new MovingAverage(4,ChronoUnit.DAYS))
        new MovingAverage(4,ChronoUnit.DAYS).hashCode() == new MovingAverage(4,ChronoUnit.DAYS).hashCode()
        new MovingAverage(4,ChronoUnit.DAYS).hashCode() != new MovingAverage(2,ChronoUnit.DAYS).hashCode()
        new MovingAverage(4,ChronoUnit.DAYS).hashCode() != new MovingAverage(4,ChronoUnit.MINUTES).hashCode()
    }
}
