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
 * Unit test for the maximum aggregation
 * @author f.lautenschlager
 */
class OutlierTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Out");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()


        when:
        def result = new Outlier().execute(ts)
        then:
        result == 1.0
    }

    def "test execute with a time series that has no outlier"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Out");
        10.times {
            timeSeries.point(it, 4711)
        }
        MetricTimeSeries ts = timeSeries.build()

        when:
        def result = new Outlier().execute(ts)
        then:
        result == -1.0
    }

    def "test execute with empty time series"() {
        when:
        def result = new Outlier().execute(new MetricTimeSeries.Builder("Out").build())
        then:
        result == -1.0
    }

    def "test exception behaviour"() {
        when:
        new Outlier().execute(new MetricTimeSeries[0])
        then:
        thrown IllegalArgumentException.class
    }

    def "test need subquery"() {
        expect:
        !new Outlier().needSubquery()
        new Outlier().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Outlier().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Outlier().getType() == AnalysisType.OUTLIER
    }

}
