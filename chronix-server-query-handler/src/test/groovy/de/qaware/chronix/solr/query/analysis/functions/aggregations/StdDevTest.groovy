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
 * Unit test for the standard deviation aggregation
 * @author f.lautenschlager
 */
class StdDevTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Stddev");
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionValueMap(1, 1, 1);

        when:
        new StdDev().execute(ts, analysisResult)
        then:
        analysisResult.getAggregationValue(0) == 3001.381363790528d
    }


    def "test for empty time series"() {
        given:
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new StdDev().execute(new MetricTimeSeries.Builder("Empty").build(), analysisResult)
        then:
        analysisResult.getAggregationValue(0) == Double.NaN
    }

    def "test arguments"() {
        expect:
        new StdDev().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new StdDev().getType() == FunctionType.DEV
    }

    def "test equals and hash code"() {
        expect:
        def stdDev = new StdDev();
        !stdDev.equals(null)
        !stdDev.equals(new Object())
        stdDev.equals(stdDev)
        stdDev.equals(new StdDev())
        new StdDev().hashCode() == new StdDev().hashCode()
    }

}
