/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query

import spock.lang.Specification

/**
 * Unit test for the Chronix Query Params class
 * Only for test coverage purposes
 * @author f.lautenschlager
 */
class ChronixQueryParamsTest extends Specification {

    def "Test private constructor"() {
        when:
        ChronixQueryParams.newInstance()

        then:
        noExceptionThrown()
    }
}
