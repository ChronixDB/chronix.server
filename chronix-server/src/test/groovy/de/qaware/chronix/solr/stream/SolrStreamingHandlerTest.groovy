/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.stream

import org.apache.solr.common.SolrDocument
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the solr streaming handler
 * @author f.lautenschlager
 */
class SolrStreamingHandlerTest extends Specification {

    def "test streamDocListInfo"() {
        given:
        def solrDocumentHandler = new SolrStreamingHandler()
        solrDocumentHandler.init(2, 0)

        when:
        solrDocumentHandler.streamDocListInfo(docsFound, 0, 0.2f)

        then:
        solrDocumentHandler.canPoll() == results

        where:
        docsFound << [0, 1]
        results << [false, true]
    }

    @Unroll
    def "test streamSolrDocument stream #streams, polls #polls, read next document #results"() {
        given:
        def solrDocumentHandler = new SolrStreamingHandler()
        def nrOfTimeSeriesPerBatch = 20

        when:
        solrDocumentHandler.init(nrOfTimeSeriesPerBatch, 0)

        solrDocumentHandler.streamDocListInfo(nrFoundForQuery, 0, 0.2f)

        streams.times {
            solrDocumentHandler.streamSolrDocument(new SolrDocument())
        }

        polls.times {
            solrDocumentHandler.poll()
        }

        then:
        results == solrDocumentHandler.canPoll()

        where:
        nrFoundForQuery << [1, 0, 0, 1, 40]
        streams << [1, 0, 0, 1, 20]
        polls << [1, 1, 0, 0, 0]
        results << [false, false, false, true, true]

    }
}
