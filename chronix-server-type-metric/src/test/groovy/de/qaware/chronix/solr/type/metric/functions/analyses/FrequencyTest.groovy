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
package de.qaware.chronix.solr.type.metric.functions.analyses

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.server.types.ChronixTimeSeries
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the frequency analysis
 * @author f.lautenschlager
 */
class FrequencyTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Freq", "metric")
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
        def tsList = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("key", ts)))

        when:
        def frequency = new Frequency()
        frequency.setArguments([windowSize, windowThreshold] as String[])
        frequency.execute(tsList, analysisResult)

        then:
        // analysisResult contains none if the threshold is bigger than the size
        analysisResult.getContextFor("key").getAnalysisValue(0) == detected

        where:
        windowSize << [20, 5]
        windowThreshold << [6, 6]
        detected << [false, true]
        analysisResult << [new FunctionCtx(1, 1, 1), new FunctionCtx(1, 1, 1)]
    }

    def "test subquery"() {
        expect:
        Frequency frequency = new Frequency()
        frequency.setArguments(["10", "6"] as String[])
        !frequency.needSubquery()
        frequency.getSubquery() == null
    }

    def "test arguments"() {
        expect:
        Frequency frequency = new Frequency()
        frequency.setArguments(["10", "6"] as String[])
        frequency.getArguments().length == 2
    }

    def "test type"() {
        expect:
        Frequency frequency = new Frequency()
        frequency.setArguments(["5", "20"] as String[])
        frequency.getQueryName() == "frequency"
    }

    @Shared
    def freq1 = new Frequency()

    @Shared
    def freq2 = new Frequency()

    @Unroll
    def "test equals and hash code"() {
        when:
        setArgs()

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
        setArgs << [{ ->
                        freq1.setArguments(["5", "20"] as String[])
                        freq2.setArguments(["5", "20"] as String[])
                    },
                    { ->
                        freq1.setArguments(["5", "20"] as String[])
                        freq2.setArguments(["6", "20"] as String[])
                    },
                    { ->
                        freq1.setArguments(["5", "20"] as String[])
                        freq2.setArguments(["5", "21"] as String[])
                    }]

        result << [true, false, false]
    }

    def "test to string"() {
        when:
        Frequency frequency = new Frequency()
        frequency.setArguments(["5", "20"] as String[])
        def stringRepresentation = frequency.toString()
        then:
        stringRepresentation.contains("5")
        stringRepresentation.contains("20")
    }
}
