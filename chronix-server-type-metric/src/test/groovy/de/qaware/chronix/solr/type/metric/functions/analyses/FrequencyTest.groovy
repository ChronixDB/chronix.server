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
package de.qaware.chronix.solr.type.metric.functions.analyses

import de.qaware.chronix.server.functions.FunctionValueMap
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
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Freq")
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
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Frequency([windowSize, windowThreshold] as String[]).execute(ts, analysisResult)
        then:
        analysisResult.getAnalysisValue(0) == detected

        where:
        windowSize << [20, 5]
        windowThreshold << [6, 6]
        detected << [false, true]
    }

    def "test subquery"() {
        expect:
        !new Frequency(["10", "6"] as String[]).needSubquery()
        new Frequency(["10", "6"] as String[]).getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Frequency(["10", "6"] as String[]).getArguments().length == 2
    }

    def "test type"() {
        expect:
        new Frequency(["5", "20"] as String[]).getQueryName() == "frequency"
    }

    def "test equals and hash code"() {
        when:
        def equals = freq1.equals(freq2)
        def dtw1Hash = freq1.hashCode()
        def dtw2Hash = freq2.hashCode()

        then:
        freq1.equals(freq1)
        !freq1.equals(new Object())
        !freq1.equals(null)
        equals == result
        dtw1Hash == dtw2Hash == result

        where:
        freq1 << [new Frequency(["5", "20"] as String[]), new Frequency(["5", "20"] as String[]), new Frequency(["5", "20"] as String[])]
        freq2 << [new Frequency(["5", "20"] as String[]), new Frequency(["6", "20"] as String[]), new Frequency(["5", "21"] as String[])]

        result << [true, false, false]
    }

    def "test to string"() {
        when:
        def stringRepresentation = new Frequency(["5", "20"] as String[]).toString()
        then:
        stringRepresentation.contains("5")
        stringRepresentation.contains("20")
    }
}
