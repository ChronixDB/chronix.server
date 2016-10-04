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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the vector transformation
 * @author f.lautenschlager
 */
class VectorizationTest extends Specification {

    @Shared
    def vectorization = new Vectorization(0.01f)

    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Vector")

        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        vectorization.execute(timeSeries, analysisResult)

        then:
        timeSeries.size() == 2
    }

    def "test transform - 0 points"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Vector")

        def timeSeries = timeSeriesBuilder.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        vectorization.execute(timeSeries, analysisResult)

        then:
        timeSeries.size() == 0
    }

    def "test transform - 1..3 Points"() {
        given:
        def timeSeriesBuilder1 = new MetricTimeSeries.Builder("Vector")
        def timeSeriesBuilder2 = new MetricTimeSeries.Builder("Vector")
        def timeSeriesBuilder3 = new MetricTimeSeries.Builder("Vector")

        def now = Instant.now()
        def analysisResult = new FunctionValueMap(1, 1, 3);

        when:
        1.times {
            timeSeriesBuilder1.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        2.times {
            timeSeriesBuilder2.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        3.times {
            timeSeriesBuilder3.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }
        def ts1 = timeSeriesBuilder1.build()
        def ts2 = timeSeriesBuilder2.build()
        def ts3 = timeSeriesBuilder3.build()

        vectorization.execute(ts1, analysisResult)
        vectorization.execute(ts2, analysisResult)
        vectorization.execute(ts3, analysisResult)

        then:
        ts1.size() == 1
        ts2.size() == 2
        ts3.size() == 3
    }

    def "test type"() {
        when:
        def vectorization = new Vectorization(0.01f);
        then:
        vectorization.getType() == FunctionType.VECTOR
    }

    def "test equals and hash code"() {
        expect:
        def function = new Vectorization(4);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Vectorization(4))
        new Vectorization(4).hashCode() == new Vectorization(4).hashCode()
        new Vectorization(4).hashCode() != new Vectorization(2).hashCode()
    }

    def "test string representation"() {
        expect:
        def string = new Vectorization(4).toString()
        string.contains("tolerance")
    }
}
