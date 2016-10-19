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
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the integral function
 * @author f.lautenschlager
 */
class IntegralTest extends Specification {
    def "test get last value"() {

        given:
        def timeSeries = new MetricTimeSeries.Builder("Integral")

        10.times {
            timeSeries.point(it + 1, it)
        }
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Integral().execute(timeSeries.build(), analysisResult)

        then:
        analysisResult.getAggregationValue(0) == 36.000022888183594d
    }

    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new Integral().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }


    def "test arguments"() {
        expect:
        new Integral().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Integral().getType() == FunctionType.INTEGRAL
    }

    def "test equals and hash code"() {
        expect:
        def last = new Integral();
        !last.equals(null)
        !last.equals(new Object())
        last.equals(last)
        last.equals(new Integral())
        new Integral().hashCode() == new Integral().hashCode()
    }
}
