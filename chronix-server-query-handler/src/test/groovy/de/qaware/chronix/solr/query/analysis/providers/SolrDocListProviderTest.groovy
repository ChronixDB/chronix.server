/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.providers

import spock.lang.Specification

/**
 * A unit test that is only used to increase test coverage.
 * The solr doc list provider uses a solr class that is tests
 * @author f.lautenschlager
 */
class SolrDocListProviderTest extends Specification {

    def "test doSimpleQuery"() {
        when:
        new SolrDocListProvider().doSimpleQuery("", null, 0, 99);

        then:
        thrown NullPointerException
    }

    def "test "() {
        when:
        new SolrDocListProvider().docListToSolrDocumentList(null, null, null, null);
        then:
        thrown NullPointerException
    }
}
