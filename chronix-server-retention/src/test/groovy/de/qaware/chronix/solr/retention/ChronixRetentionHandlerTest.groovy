/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.retention

import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import spock.lang.Specification

/**
 * Unit test for the retention handler
 * author f.lautenschlager
 */
class ChronixRetentionHandlerTest extends Specification {

    def "test init retention handler"() {
        given:
        def chronixRetentionHandler = new ChronixRetentionHandler();
        def namedList = new NamedList()
        def invariants = new NamedList()

        invariants.add("queryField", "end")
        invariants.add("timeSeriesAge", "40DAY")
        invariants.add("removeDailyAt", 12)
        invariants.add("retentionUrl", "http://localhost:8983/solr/chronix/retention")
        invariants.add("optimizeAfterDeletion", false)
        invariants.add("softCommit", false)

        namedList.add("invariants", invariants)

        when:
        chronixRetentionHandler.init(namedList)

        then:
        chronixRetentionHandler.queryField == "end"
        chronixRetentionHandler.timeSeriesAge == "40DAY"
        chronixRetentionHandler.removeDailyAt == 12
        chronixRetentionHandler.retentionURL == "http://localhost:8983/solr/chronix/retention"
        chronixRetentionHandler.softCommit == false
    }


    def "test test handle request body"() {
        given:
        def chronixRetentionHandler = new ChronixRetentionHandler();
        def namedList = new NamedList()
        def invariants = new NamedList()

        invariants.add("queryField", "end")
        invariants.add("timeSeriesAge", "40DAY")
        invariants.add("removeDailyAt", 12)
        invariants.add("retentionUrl", "http://localhost:8983/solr/chronix/retention")
        invariants.add("optimizeAfterDeletion", false)
        invariants.add("softCommit", false)

        namedList.add("invariants", invariants)

        when:
        chronixRetentionHandler.init(namedList)
        chronixRetentionHandler.handleRequestBody(Mock(SolrQueryRequest), Mock(SolrQueryResponse))

        then:
        thrown NullPointerException
    }

    def "test get description and source"() {
        given:
        def chronixRetentionHandler = new ChronixRetentionHandler();

        when:
        def description = chronixRetentionHandler.getDescription()
        def source = chronixRetentionHandler.getSource()

        then:
        description == "The Chronix retention plugin."
        source == "www.chronix.io"
    }
}
