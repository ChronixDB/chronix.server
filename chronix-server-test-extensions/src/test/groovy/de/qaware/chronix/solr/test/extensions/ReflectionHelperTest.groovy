/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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
        thrown NoSuchFieldException

    }
}
