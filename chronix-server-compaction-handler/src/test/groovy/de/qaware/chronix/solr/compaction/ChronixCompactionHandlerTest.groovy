/*
 * Copyright (C) 2016 QAware GmbH
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
package de.qaware.chronix.solr.compaction

import org.apache.lucene.document.Document
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.search.SolrIndexSearcher
import spock.lang.Specification

/**
 * Test case for {@link ChronixCompactionHandler}.
 *
 * @author alex.christ
 */
class ChronixCompactionHandlerTest extends Specification {
    ChronixCompactionHandler handler
    SolrUpdateService updateService
    LazyDocumentLoader loader
    LazyCompactor compactor
    SolrQueryRequest req
    SolrQueryResponse rsp
    SolrParams params

    def setup() {
        req = Mock()
        rsp = Mock()
        params = Mock()
        updateService = Mock()
        loader = Mock()
        compactor = Mock()
        req.getSearcher() >> Mock(SolrIndexSearcher)
        req.getParams() >> params
        handler = new ChronixCompactionHandler(mockConfig())
    }

    def "test default constructor"() {
        expect:
        new ChronixCompactionHandler() != null
    }

    def "test get description"() {
        expect:
        handler.getDescription() != null
    }

    def "test simple request"() {
        given:
        def docs = [new Document()] as Set
        def compacted = [new SolrInputDocument()] as Set
        compactor.compact(_, _) >> [new CompactionResult(docs, compacted)]
        params.get('metrics') >> 'heap_usage'

        when:
        handler.handleRequestBody(req, rsp)

        then:
        1 * rsp.add('heap_usage-numCompacted', _)
        1 * rsp.add('heap_usage-numNewDocs', _)
        1 * updateService.delete(docs[0])
        1 * updateService.add(compacted[0])
    }

    def mockConfig() {
        new CompactionHandlerConfiguration() {
            SolrUpdateService solrUpdateService(SolrQueryRequest req, SolrQueryResponse rsp) {
                return ChronixCompactionHandlerTest.this.updateService
            }

            LazyDocumentLoader documentLoader() {
                return ChronixCompactionHandlerTest.this.loader
            }

            LazyCompactor compactor() {
                return ChronixCompactionHandlerTest.this.compactor
            }
        }
    }
}