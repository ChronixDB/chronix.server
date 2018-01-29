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
package de.qaware.chronix.solr.type.metric.functions.aggregations

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.server.types.ChronixTimeSeries
import de.qaware.chronix.solr.type.metric.ChronixMetricTimeSeries
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the percentile aggregation
 * @author f.lautenschlager
 */
class PercentileTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("P","metric")
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts =timeSeries.build()
        def analysisResult = new FunctionCtx(1, 1, 1)
        when:
        Percentile percentile =  new Percentile()
        percentile.setArguments(["0.5"] as String[])
        percentile.execute(new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", ts))), analysisResult)
        then:
        analysisResult.getContextFor("").getAggregationValue(0) == 50.0d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionCtx(1, 1, 1)
        when:
        Percentile percentile = new Percentile()
        percentile.setArguments(["0.5"] as String[])
        percentile.execute(new ArrayList<ChronixTimeSeries<MetricTimeSeries>>(Arrays.asList(new ChronixMetricTimeSeries("", new MetricTimeSeries.Builder("Empty","metric").build()))), analysisResult)

        then:
        analysisResult.getContextFor("").getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        Percentile percentile = new Percentile()
        percentile.setArguments(["0.5"] as String[])
        percentile.getArguments().size() == 1
    }

    def "test type"() {
        expect:
        Percentile percentile = new Percentile()
        percentile.setArguments(["0.5"] as String[])
        percentile.getQueryName() == "p"
    }

    @Shared
    def p1 = new Percentile()

    @Shared
    def p2 = new Percentile()

    @Unroll
    def "test equals and hash code"() {
        when:
        setArgs()

        def equals = p1.equals(p2)
        def p1Hash = p1.hashCode()
        def p2Hash = p2.hashCode()

        then:
        p1.equals(p1)
        !p1.equals(new Object())
        !p1.equals(null)
        equals == result
        p1Hash == p2Hash == result

        where:
        setArgs << [{ ->
                         p1.setArguments(["0.1"] as String[])
                         p2.setArguments(["0.1"] as String[])
                         },
                     { ->
                         p1.setArguments(["0.2"] as String[])
                         p2.setArguments(["0.1"] as String[])
                     }]

        result << [true, false]
    }

    def "test to string"() {
        when:
        Percentile percentile = new Percentile()
        percentile.setArguments(["0.2"] as String[])
        def stringRepresentation = percentile.toString()
        then:
        stringRepresentation.contains("0.2")
    }

}
