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
 * Unit test for the minimum aggregation
 * @author f.lautenschlager
 */
class MinTest extends Specification {
    def "test execute with negative"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Min");
        10.times {
            timeSeries.point(it, it * -10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Min().execute(ts)
        then:
        result == -90.0
    }

    def "test execute with positive number"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Min");
        10.times {
            timeSeries.point(it, it * 10 + 1)
        }
        timeSeries.point(11, 0)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Min().execute(ts)
        then:
        result == 0.0
    }

    def "test for empty time series"() {
        when:
        def result = new Min().execute([new MetricTimeSeries.Builder("Empty").build()] as MetricTimeSeries[])
        then:
        result == Double.NaN
    }

    def "test exception behaviour"() {
        when:
        new Min().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        !new Min().needSubquery()
        new Min().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Min().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Min().getType() == FunctionType.MIN
    }

    def "test equals and hash code"() {
        expect:
        def min = new Min();
        !min.equals(null)
        !min.equals(new Object())
        min.equals(min)
        min.equals(new Min())
        new Min().hashCode() == new Min().hashCode()
    }
}
