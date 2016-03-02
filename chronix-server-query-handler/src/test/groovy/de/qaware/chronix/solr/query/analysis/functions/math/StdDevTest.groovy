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
