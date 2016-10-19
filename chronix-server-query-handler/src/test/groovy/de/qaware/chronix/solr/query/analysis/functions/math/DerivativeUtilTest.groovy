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
