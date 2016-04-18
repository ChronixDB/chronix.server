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

import de.qaware.chronix.solr.query.analysis.functions.AnalysisType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Range analysis unit test
 * @author f.lautenschlager
 */
class RangeTest extends Specification {

    def "test range analysis"() {
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Range");
        10.times {
            timeSeries.point(it, it)
        }
        timeSeries.point(11, -5)

        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Range().execute(ts)
        then:
        result == 14.0d

    }

    def "test for empty time series"() {
        when:
        def result = new Range().execute([new MetricTimeSeries.Builder("Empty").build()] as MetricTimeSeries[])
        then:
        result == Double.NaN
    }


    def "test exception behaviour"() {
        when:
        new Range().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        !new Range().needSubquery()
        new Range().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Range().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Range().getType() == AnalysisType.RANGE
    }

}
