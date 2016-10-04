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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the last function
 * @author f.lautenschlager
 */
class LastTest extends Specification {

    def "test get last value"() {

        given:
        def timeSeries = new MetricTimeSeries.Builder("Last-Time-Series")

        10.times {
            timeSeries.point(10 - it, it)
        }
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Last().execute(timeSeries.build(), analysisResult)

        then:
        analysisResult.getAggregationValue(0) == 0d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Last().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Last().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Last().getType() == FunctionType.LAST
    }

    def "test equals and hash code"() {
        expect:
        def last = new Last();
        !last.equals(null)
        !last.equals(new Object())
        last.equals(last)
        last.equals(new Last())
        new Last().hashCode() == new Last().hashCode()
    }
}
