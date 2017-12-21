/*
 * Copyright (C) 2018 QAware GmbH
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
