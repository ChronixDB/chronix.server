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
package de.qaware.chronix.solr.query.analysis.functions

import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the frequency analysis
 * @author f.lautenschlager
 */
class FrequencyTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Freq");
        def start = Instant.now()

        //First add a window with normal values
        10.times {
            timeSeries.point(start.plus(it, ChronoUnit.MINUTES).toEpochMilli(), it * 10)
        }
        def startOfHighFrequency = start.plus(11, ChronoUnit.MINUTES)
        //Lets insert some points in higher frequency
        timeSeries.point(startOfHighFrequency.toEpochMilli(), 1)
        timeSeries.point(startOfHighFrequency.plus(10, ChronoUnit.SECONDS).toEpochMilli(), 2)
        timeSeries.point(startOfHighFrequency.plus(15, ChronoUnit.SECONDS).toEpochMilli(), 3)
        timeSeries.point(startOfHighFrequency.plus(20, ChronoUnit.SECONDS).toEpochMilli(), 4)
        timeSeries.point(startOfHighFrequency.plus(25, ChronoUnit.SECONDS).toEpochMilli(), 5)
        timeSeries.point(startOfHighFrequency.plus(30, ChronoUnit.SECONDS).toEpochMilli(), 6)
        timeSeries.point(startOfHighFrequency.plus(35, ChronoUnit.SECONDS).toEpochMilli(), 7)
        timeSeries.point(startOfHighFrequency.plus(40, ChronoUnit.SECONDS).toEpochMilli(), 8)
        timeSeries.point(startOfHighFrequency.plus(45, ChronoUnit.SECONDS).toEpochMilli(), 9)
        timeSeries.point(startOfHighFrequency.plus(50, ChronoUnit.SECONDS).toEpochMilli(), 10)
        timeSeries.point(startOfHighFrequency.plus(55, ChronoUnit.SECONDS).toEpochMilli(), 11)
        timeSeries.point(startOfHighFrequency.plus(60, ChronoUnit.SECONDS).toEpochMilli(), 12)

        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Frequency(windowSize, windowThreshold).execute(ts)
        then:
        result == detected

        where:
        windowSize << [20, 5]
        windowThreshold << [6, 6]
        detected << [-1, 1]
    }

    def "test exception behaviour"() {
        when:
        new Frequency(10, 6).execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        !new Frequency(10, 6).needSubquery()
        new Frequency(10, 6).getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Frequency(10, 6).getArguments().length == 2
    }

    def "test type"() {
        expect:
        new Frequency(5, 20).getType() == AnalysisType.FREQUENCY
    }
}
