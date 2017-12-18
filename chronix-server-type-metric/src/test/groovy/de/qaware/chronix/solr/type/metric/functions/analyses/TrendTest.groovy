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
package de.qaware.chronix.solr.type.metric.functions.analyses

import de.qaware.chronix.server.functions.FunctionCtx
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the trend analysis
 * @author f.lautenschlager
 */
class TrendTest extends Specification {
    def "test execute"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Trend","metric")
        10.times {
            timeSeries.point(it, it * 10)
        }
        timeSeries.point(11, 9999)
        MetricTimeSeries ts = timeSeries.build()
        def analysisResult = new FunctionCtx(1, 1, 1)

        when:
        new Trend().execute(ts, analysisResult)
        then:
        analysisResult.getAnalysisValue(0)
    }

    def "test need subquery"() {
        expect:
        !new Trend().needSubquery()
        new Trend().getSubquery() == null
    }

    def "test arguments"() {
        expect:
        new Trend().getArguments().length == 0
    }

    def "test type"() {
        expect:
        new Trend().getQueryName() == "trend"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Trend()
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Trend())
        new Trend().hashCode() == new Trend().hashCode()
    }
}
