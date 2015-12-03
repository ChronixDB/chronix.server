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

        def doubles = [0.5d, 1.4d, 10.2d, 40.2d]

        when:
        def result = StdDev.dev(doubles)

        then:
        result == 18.605263592148685d
    }

    def "test dev with zero"() {
        given:

        def doubles = []

        when:
        def result = StdDev.dev(doubles)

        then:
        result == Double.NaN;
    }
}
