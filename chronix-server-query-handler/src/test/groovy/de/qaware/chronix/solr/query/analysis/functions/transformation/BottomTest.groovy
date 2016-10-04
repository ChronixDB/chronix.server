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

import de.qaware.chronix.solr.query.analysis.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.FunctionType
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification

/**
 * Unit test for the bottom transformation
 * @author f.lautenschlager
 */
class BottomTest extends Specification {
    def "test transform"() {
        given:
        def bottom = new Bottom(4)
        def analysisResult = new FunctionValueMap(1, 1, 1);

        def timeSeriesBuilder = new MetricTimeSeries.Builder("Bottom")
        timeSeriesBuilder.point(1, 5d)
        timeSeriesBuilder.point(2, 99d)
        timeSeriesBuilder.point(3, 3d)
        timeSeriesBuilder.point(4, 5d)
        timeSeriesBuilder.point(5, 65d)
        timeSeriesBuilder.point(6, 23d)

        def timeSeries = timeSeriesBuilder.build()
        when:
        bottom.execute(timeSeries, analysisResult)


        then:
        timeSeries.size() == 4
        timeSeries.getValue(0) == 3d
        timeSeries.getValue(1) == 5d
        timeSeries.getValue(2) == 5d
        timeSeries.getValue(3) == 23d

    }

    def "test getType"() {
        when:
        def bottom = new Bottom(2)
        then:
        bottom.getType() == FunctionType.BOTTOM
    }

    def "test getArguments"() {
        when:
        def bottom = new Bottom(2)
        then:
        bottom.getArguments()[0] == "value=2"
    }

    def "test equals and hash code"() {
        expect:
        def function = new Bottom(4);
        !function.equals(null)
        !function.equals(new Object())
        function.equals(function)
        function.equals(new Bottom(4))
        new Bottom(4).hashCode() == new Bottom(4).hashCode()
        new Bottom(4).hashCode() != new Bottom(2).hashCode()
    }

    def "test string representation"() {
        expect:
        def string = new Bottom(4).toString()
        string.contains("value")
    }
}
