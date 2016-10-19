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

import de.qaware.chronix.converter.common.DoubleList
import de.qaware.chronix.converter.common.LongList
import spock.lang.Specification

/**
 * Unit test for the regression class
 * @author f.lautenschlager
 */
class LinearRegressionTest extends Specification {
    def "test slope"() {
        given:
        def times = new LongList()
        def values = new DoubleList()
        100.times {
            times.add(it as long)
            values.add(it * 2 as double)
        }

        when:
        def slope = new LinearRegression(times, values).slope()

        then:
        slope == 2d
    }
}
