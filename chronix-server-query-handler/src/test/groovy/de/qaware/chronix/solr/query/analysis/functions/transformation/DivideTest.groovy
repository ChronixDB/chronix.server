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
package de.qaware.chronix.solr.query.analysis.functions.transformation

import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the divide transformation
 * @author f.lautenschlager
 */
class DivideTest extends Specification {

    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Div")
        def now = Instant.now()

        100.times {
            timeSeriesBuilder.point(now.plus(it, ChronoUnit.SECONDS).toEpochMilli(), it + 1)
        }

        def divide = new Divide(2);

        when:
        divide.getArguments()[0] == "factor=2.0"
        def dividedTimeSeries = divide.transform(timeSeriesBuilder.build())

        then:
        100.times {
            dividedTimeSeries.getValue(it) == (it + 1) / 2d
        }
    }

    def "test getType"() {
        when:
        def divide = new Divide(2);
        then:
        divide.getType() == FunctionType.DIVIDE
    }

    def "test getArguments"() {
        when:
        def divide = new Divide(2);
        then:
        divide.getArguments()[0] == "factor=2.0"
    }
    def "test equals and hash code"() {
        expect:
        def function = new Divide(4);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Divide(4))
        new Divide(4).hashCode() == new Divide(4).hashCode()
        new Divide(4).hashCode() != new Divide(2).hashCode()
    }
}
