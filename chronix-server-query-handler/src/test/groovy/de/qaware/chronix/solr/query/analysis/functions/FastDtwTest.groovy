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
package de.qaware.chronix.solr.query.analysis.functions

import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the fast dtw analysis
 * @author f.lautenschlager
 */
class FastDtwTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("FastDTW");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts1 = timeSeries.build()
        MetricTimeSeries ts2 = timeSeries.build()


        when:
        def result = new FastDtw("", 5, 20).execute(ts1, ts2)
        then:
        result == 0
    }

    def "test exception behaviour"() {
        when:
        new FastDtw("", 5, 20).execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test subquery"() {
        expect:
        new FastDtw("", 5, 20).needSubquery()
        new FastDtw("", 5, 20).getArguments().size() == 3
        new FastDtw("(query)", 5, 20).getSubquery().equals("(query)")
    }

    def "test arguments"() {
        expect:
        new Outlier().getArguments().length == 0
    }
}
