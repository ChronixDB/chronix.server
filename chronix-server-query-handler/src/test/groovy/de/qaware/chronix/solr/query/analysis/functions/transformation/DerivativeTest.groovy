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

/**
 * Unit test for the derivative transformation
 *
 * @author f.lautenschlager
 */
class DerivativeTest extends Specification {
    def "test transform"() {
        given:
        def timeSeriesBuilder = new MetricTimeSeries.Builder("Derivative time series")
        def derivative = new Derivative()

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:00.000Z"), 5)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:01.000Z"), 4)

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:06.500Z"), 6)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:07.000Z"), 10)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:08.000Z"), 31)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:09.000Z"), 9)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:10.000Z"), 2)

        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:15.000Z"), 1)
        timeSeriesBuilder.point(dateOf("2016-05-23T10:51:16.000Z"), 5)


        when:
        def derivativeTimeSeries = derivative.transform(timeSeriesBuilder.build())

        then:
        derivativeTimeSeries.size() == 7
    }

    def long dateOf(def format) {
        Instant.parse(format as String).toEpochMilli()
    }

    def "test getType"() {
        expect:
        new Derivative().getType() == FunctionType.DERIVATIVE;
    }

    def "test getArguments"() {
        expect:
        new Derivative().getArguments() == new String[0]
    }
}
