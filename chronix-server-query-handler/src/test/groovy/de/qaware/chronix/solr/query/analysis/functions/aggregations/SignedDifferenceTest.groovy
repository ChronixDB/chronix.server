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
 * Unit test for the signed difference
 * @author f.lautenschlager
 */
class SignedDifferenceTest extends Specification {
    def "test execute with negative values"(){
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, -1)
        timeSeries.point(1, -10)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new SignedDifference().execute(ts)
        then:
        result == -9d
    }

    def "test execute with positive values"(){
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, 1)
        timeSeries.point(1, 10)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new SignedDifference().execute(ts)
        then:
        result == 9d
    }

    def "test execute with negative start and positive end"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, -1)
        timeSeries.point(1, 10)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new SignedDifference().execute(ts)
        then:
        result == 11d
    }

    def "test execute with positive start and negative end"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Signed Difference");
        timeSeries.point(0, 1)
        timeSeries.point(1, -10)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new SignedDifference().execute(ts)
        then:
        result == -11d
    }


    def "test exception behaviour"() {
        when:
        new SignedDifference().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test for empty time series"() {
        when:
        def result = new SignedDifference().execute([new MetricTimeSeries.Builder("Empty").build()] as MetricTimeSeries[])
        then:
        result == Double.NaN
    }

    def "test subquery"() {
        expect:
        !new SignedDifference().needSubquery()
        new SignedDifference().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new SignedDifference().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new SignedDifference().getType() == FunctionType.SDIFF
    }
}
