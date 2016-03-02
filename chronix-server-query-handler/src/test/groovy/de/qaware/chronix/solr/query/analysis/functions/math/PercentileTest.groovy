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

import de.qaware.chronix.timeseries.dt.DoubleList
import spock.lang.Specification

/**
 * Unit test for the percentile class
 * @author f.lautenschlager
 */
class PercentileTest extends Specification {

    def "test private constructor"() {
        when:
        Percentile.newInstance()

        then:
        noExceptionThrown()
    }

    def "test evaluate 0.5 percentile"() {
        given:
        def percentile = 0.5

        when:
        def value = Percentile.evaluate(points, percentile)

        then:
        value == expected

        where:
        points << [twoPoints(),onePoint()]
        expected << [10.5, 10]
    }

    def onePoint() {
        def values = new DoubleList()
        values.add(10)
        return values
    }

    def twoPoints() {
        def values = new DoubleList()
        values.add(10)
        values.add(11)
        return values
    }
}
