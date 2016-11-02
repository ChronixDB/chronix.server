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
package de.qaware.chronix.solr

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.util.NamedList
import spock.lang.Shared
import spock.lang.Specification

import static de.qaware.chronix.Schema.*
import static de.qaware.chronix.converter.common.MetricTSSchema.METRIC
import static de.qaware.chronix.solr.SolrServerFactory.newEmbeddedSolrServer
import static de.qaware.chronix.solr.TestUtils.*
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.*
import static java.lang.Math.sin
import static org.apache.solr.common.params.CommonParams.Q
import static org.apache.solr.common.params.CommonParams.QT

/**
 * Integration test for {@link de.qaware.chronix.solr.compaction.ChronixCompactionHandler}.
 *
 * @author alex.christ
 */
class CompactionHandlerTestIT extends Specification {

    @Shared
    def EmbeddedSolrServer solr

    def setupSpec() {
        solr = newEmbeddedSolrServer()
    }

    def setup() {
        solr.deleteByQuery("*:*")
        solr.commit()
    }

    def cleanupSpec() {
        solr.deleteByQuery("*:*")
        solr.commit()
        solr.close()
    }

    def "test compaction of a small number of documents"() {
        given:
        solr.add([doc((START): 1, (END): 2, (METRIC): 'cpu', (DATA): compress(1L: 10d, 2L: 20d)),
                  doc((START): 3, (END): 4, (METRIC): 'cpu', (DATA): compress(3L: 30d, 4L: 40d)),
                  doc((START): 5, (END): 6, (METRIC): 'cpu', (DATA): compress(5L: 50d, 6L: 60d))])
        solr.commit()
        def compactionQuery = new QueryRequest(params((QT): '/compact', (JOIN_KEY): 'metric', (PAGE_SIZE): 8, (CHUNK_SIZE): 10))
        def allDocsQuery = new QueryRequest(params((QT): '/select', (Q): '*:*'))

        when:
        def rsp = solr.request(compactionQuery)
        NamedList rspParts = rsp.get('responseHeader')

        then:
        rspParts.get('status') == 0
        SolrDocumentList foundDocs = solr.request(allDocsQuery).get('response')

        expect:
        foundDocs.size() == 1
        decompress(foundDocs[0].get(DATA), 1, 6) == [1L: 10d, 2L: 20d, 3L: 30d, 4L: 40d, 5L: 50d, 6L: 60d]
    }

    def "test compaction of a large number of documents"() {
        given:
        (1L..100L).step(5) { def start ->
            def end = start + 4
            def data = (start..end).collectEntries { [it, sin(it)] }
            solr.add(doc((START): start, (END): end, (METRIC): 'cpu', (DATA): compress(data)))
        }
        solr.commit()
        def compactionQuery = new QueryRequest(params((QT): '/compact', (JOIN_KEY): 'metric', (PAGE_SIZE): 10, (CHUNK_SIZE): 100))
        def allDocsQuery = new QueryRequest(params((QT): '/select', (Q): '*:*'))

        when:
        def rsp = solr.request(compactionQuery)
        NamedList rspParts = rsp.get('responseHeader')

        then:
        rspParts.get('status') == 0

        expect:
        SolrDocumentList foundDocs = solr.request(allDocsQuery).get('response')
        foundDocs.size() == 1
        decompress(foundDocs[0].get(DATA), 1, 100).entrySet().each { assert sin(it.key) == it.value }
    }
}