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
import org.apache.solr.common.util.Pair
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Unit test for the fast dtw analysis
 * @author f.lautenschlager
 */

class FastDtwTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries1 = new MetricTimeSeries.Builder("FastDTW-1", "metric")
        MetricTimeSeries.Builder timeSeries2 = new MetricTimeSeries.Builder("FastDTW-2", "metric")
        10.times {
            timeSeries1.point(it, it + 10)
            timeSeries2.point(it, it + 10)
        }
        timeSeries1.point(11, 9999)
        timeSeries2.point(11, 9999)

        timeSeries1.attribute("host", "production-01")
        MetricTimeSeries ts1 = timeSeries1.build()

        timeSeries2.attribute("host", "production-02")
        MetricTimeSeries ts2 = timeSeries2.build()

        def timeSeriesList = new ArrayList<ChronixTimeSeries<MetricTimeSeries>>()
        timeSeriesList.add(new ChronixMetricTimeSeries("production-01", ts1))
        timeSeriesList.add(new ChronixMetricTimeSeries("production-02", ts2))

        def analysisResult = new FunctionCtx(0, 1, 0)

        when:
        FastDtw fastDtw = new FastDtw()
        fastDtw.setArguments(["compare(host=production-01)", "5", "20"] as String[])

        fastDtw.execute(timeSeriesList, analysisResult)
        then:
        def entry = analysisResult.getContextFor("production-01")

        //expect this is true
        entry.getAnalysisValue(0)
    }

    @Ignore
    def "test time series with equal timestamps"() {
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW-1", "metric")
        timeSeries.point(0, 2)
        3.times {
            timeSeries.point(1, it)
        }
        timeSeries.point(2, 2)

        def ts1 = timeSeries.build()
        def analysisResult = new FunctionCtx(1, 1, 1)

        when:
        new FastDtw(["", "5", "20"] as String[]).execute(new Pair(ts1, ts1), analysisResult)

        then:
        analysisResult.getAnalysisValue(0)

    }

    @Ignore
    def "test execute for -1 as result"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW-1", "metric")
        MetricTimeSeries.Builder secondTimeSeries = new MetricTimeSeries.Builder("FastDTW-2", "metric")
        10.times {
            timeSeries.point(it, it * 10)
            secondTimeSeries.point(it, it * -10)
        }
        def ts1 = timeSeries.build()
        def ts2 = secondTimeSeries.build()
        def analysisResult = new FunctionCtx(1, 1, 1)

        when:
        new FastDtw(["", "5", "0"] as String[]).execute(new Pair(ts1, ts2), analysisResult)
        then:
        !analysisResult.getAnalysisValue(0)
    }


    @Ignore
    def "test arguments"() {
        expect:
        new FastDtw().getArguments().length == 0
    }

    @Ignore
    def "test type"() {
        expect:
        new FastDtw(["(query)", "5", "20"] as String[]).getQueryName() == "fastdtw"
    }

    @Ignore
    def "test equals and hash code"() {
        when:
        def equals = dtw1.equals(dtw2)
        def dtw1Hash = dtw1.hashCode()
        def dtw2Hash = dtw2.hashCode()

        then:
        dtw1.equals(dtw1)
        !dtw1.equals(new Object())
        !dtw1.equals(null)
        equals == result
        dtw1Hash == dtw2Hash == result

        where:
        dtw1 << [new FastDtw(["", "5", "20"] as String[]), new FastDtw(["metric:a", "5", "20"] as String[]), new FastDtw(["metric:a", "5", "20"] as String[]), new FastDtw(["metric:a", "5", "20"] as String[])]
        dtw2 << [new FastDtw(["", "5", "20"] as String[]), new FastDtw(["metric:b", "5", "20"] as String[]), new FastDtw(["metric:a", "6", "20"] as String[]), new FastDtw(["metric:a", "5", "21"] as String[])]

        result << [true, false, false, false]
    }

    @Ignore
    def "test to string"() {
        when:
        def stringRepresentation = new FastDtw(["metric:a", "5", "20"] as String[]).toString()
        then:
        stringRepresentation.contains("metric:a")
        stringRepresentation.contains("5")
        stringRepresentation.contains("20")
    }
}
