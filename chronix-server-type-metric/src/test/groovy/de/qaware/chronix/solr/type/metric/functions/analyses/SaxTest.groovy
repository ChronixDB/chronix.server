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

import de.qaware.chronix.server.functions.ChronixFunction
import de.qaware.chronix.server.functions.FunctionValueMap
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the SAX analysis
 * @author f.lautenschlager
 */
class SaxTest extends Specification {

    @Unroll
    def "test execute sax. Regex #regex expected result is #matches"() {
        given:
        def ts = new MetricTimeSeries.Builder("Sax", "metric")
        10.times {
            ts.point(it + 1, it + 1)
        }
        def analysisResult = new FunctionValueMap(1, 1, 1);

        def args = [regex, "10", "10", "0.01"] as String[]

        def sax = new Sax(args)
        when:
        sax.execute(ts.build(), analysisResult)
        then:
        analysisResult.getAnalysisValue(0) == matches
        where:

        regex << ["*af*", "*ab*", "*ij*"]
        matches << [false, true, true]

    }

    def "test execute sax with invalid arguments"() {
        given:
        def ts = new MetricTimeSeries.Builder("Sax", "metric").build()
        def analysisResult = new FunctionValueMap(1, 1, 1);
        when:
        new Sax(args).execute(ts, analysisResult)
        then:
        !analysisResult.getAnalysisValue(0)

        where:
        args << [["*af*", "7", "7", "0.01"] as String[], ["*af*", "7", "22", "0.01"] as String[]]
    }

    def "test getArguments"() {
        expect:
        Arrays.equals(new Sax(["*af*", "7", "7", "0.01"] as String[]).getArguments(),
                ["pattern=.*af.*", "paaSize=7", "alphabetSize=7", "threshold=0.01"] as String[])

    }

    def "test getType"() {
        expect:
        new Sax(["*af*", "7", "7", "0.01"] as String[]).getType() == ChronixFunction.FunctionType.ANALYSIS
    }

    def "test needSubquery"() {
        expect:
        !new Sax(["*af*", "7", "7", "0.01"] as String[]).needSubquery()
    }

    def "test getSubquery"() {
        expect:
        new Sax(["*af*", "7", "7", "0.01"] as String[]).getSubquery() == null
    }
}