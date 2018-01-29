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
    def vectorization = new Vectorization()

    def "test transform"() {
        given:
        vectorization.setArguments(["0.01"] as String[])
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Vector","metric")

        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def timeSeriesList = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeriesBuilder.build())))
        def analysisResult = new FunctionCtx(1, 1, 1);

        when:
        vectorization.execute(timeSeriesList, analysisResult)

        then:
        timeSeriesList.get(0).getRawTimeSeries().size() == 2
    }

    def "test transform - 0 points"() {
        given:
        vectorization.setArguments(["0.01"] as String[])
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Vector","metric")

        def timeSeriesList = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeriesBuilder.build())))
        def analysisResult = new FunctionCtx(1, 1, 1)

        when:
        vectorization.execute(timeSeriesList, analysisResult)

        then:
        timeSeriesList.get(0).getRawTimeSeries().size() == 0
    }

    def "test transform - 1..3 Points"() {
        given:
        vectorization.setArguments(["0.01"] as String[])
        def timeSeriesBuilder1 = new MetricTimeSeries.Builder("Vector","metric")
        def timeSeriesBuilder2 = new MetricTimeSeries.Builder("Vector","metric")
        def timeSeriesBuilder3 = new MetricTimeSeries.Builder("Vector","metric")

        def now = Instant.now()
        def analysisResult = new FunctionCtx(1, 1, 3)

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

        def tsl1 = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeriesBuilder1.build())))
        def tsl2 = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeriesBuilder2.build())))
        def tsl3 = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", timeSeriesBuilder3.build())))

        vectorization.execute(tsl1, analysisResult)
        vectorization.execute(tsl2, analysisResult)
        vectorization.execute(tsl3, analysisResult)

        then:
        tsl1.get(0).getRawTimeSeries().size() == 1
        tsl2.get(0).getRawTimeSeries().size() == 2
        tsl3.get(0).getRawTimeSeries().size() == 3
    }

    def "test type"() {
        when:
        def vectorization = new Vectorization()
        vectorization.setArguments(["0.01"] as String[])
        then:
        vectorization.getQueryName() == "vector"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Vectorization()
        function.setArguments(["4"] as String[])
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)

        def functionEq = new Vectorization()
        functionEq.setArguments(["4"] as String[])
        function.equals(functionEq)

        def functionUnEq = new Vectorization()
        functionUnEq.setArguments(["2"] as String[])

        function.hashCode() == functionEq.hashCode()
        function.hashCode() != functionUnEq.hashCode()
    }

    def "test string representation"() {
        expect:
        Vectorization vec = new Vectorization()
        vec.setArguments(["4"] as String[])
        def string = vec.toString()
        string.contains("tolerance")
    }
}
