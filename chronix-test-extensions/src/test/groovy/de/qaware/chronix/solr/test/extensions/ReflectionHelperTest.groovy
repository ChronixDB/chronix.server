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
package de.qaware.chronix.solr.test.extensions

import spock.lang.Specification

/**
 * Unit test for the reflection helper class that is used in tests
 * @author f.lautenschlager
 */
class ReflectionHelperTest extends Specification {
    def "test setValueToFieldOfObject"() {
        given:
        def someClass = new SomeClass();

        when:
        def nameBefore = someClass.getName()
        def fatherBefore = someClass.getFather()
        ReflectionHelper.setValueToFieldOfObject("Chronix-Modified", "name", someClass)
        ReflectionHelper.setValueToFieldOfObject("Father-Modified", "father", someClass)
        def nameAfter = someClass.getName();
        def fatherAfter = someClass.getFather();

        then:
        nameBefore == "Chronix"
        nameAfter == "Chronix-Modified"
        fatherBefore == "Norbert"
        fatherAfter == "Father-Modified"
    }

    def "test private constructor"() {
        when:
        ReflectionHelper.newInstance()

        then:
        noExceptionThrown()
    }

    def "test exception case"() {
        given:
        def someClass = new SomeClass();

        when:
        ReflectionHelper.setValueToFieldOfObject("Father-Modified", "father-not-exists", someClass)

        then:
        thrown IllegalAccessException

    }
}
