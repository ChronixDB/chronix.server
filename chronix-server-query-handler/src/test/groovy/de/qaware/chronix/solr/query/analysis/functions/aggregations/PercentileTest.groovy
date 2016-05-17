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
package de.qaware.chronix.solr.query.analysis.functions.aggregations

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the percentile aggregation
 * @author f.lautenschlager
 */
class PercentileTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("P");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Percentile(0.5).execute(ts)
        then:
        result == 50.0d
    }

    def "test exception behaviour"() {
        when:
        new Percentile(0.5).execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test for empty time series"() {
        when:
        def result = new Percentile(0.5).execute([new MetricTimeSeries.Builder("Empty").build()] as MetricTimeSeries[])
        then:
        result == Double.NaN
    }

    def "test subquery"() {
        expect:
        !new Percentile(0.5).needSubquery()
        new Percentile(0.5).getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Percentile(0.5).getArguments().size() == 1
    }

    def "test type"() {
        expect:
        new Percentile(0.5).getType() == FunctionType.P
    }

    def "test equals and hash code"() {
        when:
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
        p1 << [new Percentile(0.1), new Percentile(0.2)]
        p2 << [new Percentile(0.1), new Percentile(0.1)]

        result << [true, false]
    }

    def "test to string"() {
        when:
        def stringRepresentation = new Percentile(0.2).toString()
        then:
        stringRepresentation.contains("0.2")
    }

}
