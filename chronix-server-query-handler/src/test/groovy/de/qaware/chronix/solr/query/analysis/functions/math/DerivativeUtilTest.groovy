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
package de.qaware.chronix.solr.query.analysis.functions.math

import spock.lang.Specification

import java.time.Instant

/**
 * Unit test for the derivative util
 * @author f.lautenschlager
 */
class DerivativeUtilTest extends Specification {

    def "test private constructor"() {
        when:
        DerivativeUtil.newInstance()
        then:
        noExceptionThrown()
    }

    def "test derivative"() {
        given:
        def xT1 = 10;
        def xT = 2;

        def tsT1 = dateOf("2016-05-23T10:51:00.000Z");
        //five seconds later
        def tsT = tsT1 + 5000

        when:
        def result = DerivativeUtil.derivative(xT1, xT, tsT1, tsT)

        then:
        result == -0.8d
    }

    def long dateOf(def format) {
        Instant.parse(format as String).toEpochMilli()
    }
}
