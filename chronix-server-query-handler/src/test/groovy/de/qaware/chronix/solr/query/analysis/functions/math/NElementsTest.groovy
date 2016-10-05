/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.math

import spock.lang.Specification

/**
 * Unit test for the value top / bottom elements helper
 * @author f.lautenschlager
 */
class NElementsTest extends Specification {

    def "test private constructor"() {
        when:
        NElements.newInstance()
        then:
        noExceptionThrown()
    }

    def "test calc"() {
        given:
        def times = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as long[]
        def value = [42, 1, 3, 6, 0, 19, 4, 20, 0, 10] as double[]

        when:
        def result = NElements.calc(NElements.NElementsCalculation.TOP, 3, times, value)
        then:
        result.NTimes.length == 3
        result.NValues.length == 3

        result.NTimes[0] == 1l
        result.NTimes[1] == 8l
        result.NTimes[2] == 6l

        result.NValues[0] == 42d
        result.NValues[1] == 20d
        result.NValues[2] == 19d
    }

    def "test equals and hashCode of internal points"() {
        given:
        NElements.Pair pair = new NElements.Pair(0, 1)
        NElements.Pair pairCopy = new NElements.Pair(0, 1)
        NElements.Pair pairInvalidCopy = new NElements.Pair(1, 0)

        expect:
        pair.equals(pair)
        pair.equals(pairCopy)

        !pair.equals(null)
        !pair.equals(new Object())
        !pair.equals(pairInvalidCopy)

        pair.hashCode() == pair.hashCode()
        pair.hashCode() == pairCopy.hashCode()
        pair.hashCode() != pairInvalidCopy.hashCode()
    }

}
