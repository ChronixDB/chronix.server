/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.query.analysis.collectors.math

import de.qaware.chronix.timeseries.DoubleList
import spock.lang.Specification

/**
 * Created by f.lautenschlager on 26.11.2015.
 */
class StdDevTest extends Specification {

    def "test private constructor"() {
        when:
        StdDev.newInstance()
        then:
        noExceptionThrown()
    }

    def "test dev"() {
        given:

        def doubles = new DoubleList()
        doubles.add(0.5)
        doubles.add(1.4)
        doubles.add(10.2)
        doubles.add(40.2)

        when:
        def result = StdDev.dev(doubles)

        then:
        result == 18.605263592148685d
    }

    def "test dev with zero"() {
        given:

        def doubles = new DoubleList()

        when:
        def result = StdDev.dev(doubles)

        then:
        result == Double.NaN;
    }
}
