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
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.JOIN_KEY
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.POINTS_PER_CHUNK
import static java.lang.Math.cos
import static java.lang.Math.sin
import static org.apache.solr.common.params.CommonParams.Q
import static org.apache.solr.common.params.CommonParams.QT

/**
 * Integration test for {@link de.qaware.chronix.solr.compaction.ChronixCompactionHandler}.
 *
 * @author alex.christ
 */
class CompactionHandlerTestIT extends Specification {

    def QueryRequest ALL_DOCS_QUERY = new QueryRequest(params((QT): '/select', (Q): '*:*'))
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

    def "test status code"() {
        given:
        solr.add([doc((START): 1, (END): 2, (METRIC): 'cpu', (DATA): compress(1L: 10d, 2L: 20d))])
        solr.commit()
        def compactionQuery = new QueryRequest(params((QT): '/compact', (JOIN_KEY): METRIC, (POINTS_PER_CHUNK): 10))

        when:
        NamedList rsp = solr.request(compactionQuery).get('responseHeader')

        then:
        rsp.get('status') == 0
    }

    def "test compaction of a small number of documents"() {
        given:
        solr.add([doc((START): 1, (END): 2, (METRIC): 'cpu', (DATA): compress(1L: 10d, 2L: 20d)),
                  doc((START): 3, (END): 4, (METRIC): 'cpu', (DATA): compress(3L: 30d, 4L: 40d)),
                  doc((START): 5, (END): 6, (METRIC): 'cpu', (DATA): compress(5L: 50d, 6L: 60d))])
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): METRIC, (POINTS_PER_CHUNK): 10)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 1
        decompress(docs[0].get(DATA), 1, 6) == [1L: 10d, 2L: 20d, 3L: 30d, 4L: 40d, 5L: 50d, 6L: 60d]
    }

    def "test compaction of a large number of documents"() {
        given:
        (1L..100L).collate(5).each { range ->
            def data = range.collectEntries { it -> [it, sin(it)] }
            solr.add(doc((START): range.first(), (END): range.last(), (METRIC): 'cpu', (DATA): compress(data)))
        }
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): METRIC, (POINTS_PER_CHUNK): 100)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 1
        decompress(docs[0].get(DATA), 1, 100).entrySet().each { assert sin(it.key) == it.value }
    }

    def "test special chars as value of a join key field"() {
        given:
        solr.add([doc((START): 5, (END): 6, (METRIC): 'a:AND() {!$#}\\', (DATA): compress(1L: 10d))])
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): METRIC, (POINTS_PER_CHUNK): 10)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 1
        docs[0].get(METRIC) == 'a:AND() {!$#}\\'
        decompress(docs[0].get(DATA), 1, 6) == [1L: 10d]
    }

    def "test widow handdling"() {
        given:
        (1L..10L).collate(5).each { range ->
            def data = range.collectEntries { it -> [it, sin(it)] }
            solr.add(doc((START): range.first(), (END): range.last(), (METRIC): 'cpu', (DATA): compress(data)))
        }
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): METRIC, (POINTS_PER_CHUNK): 4)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 3
    }

    def "test compaction of two time series"() {
        given:
        (1L..100L).collate(5).each { def range ->
            def cpuData = range.collectEntries { it -> [it, sin(it)] }
            def heapData = range.collectEntries { it -> [it, cos(it)] }
            solr.add(doc((START): range.first(), (END): range.last(), (METRIC): 'cpu', (DATA): compress(cpuData)))
            solr.add(doc((START): range.first(), (END): range.last(), (METRIC): 'heap', (DATA): compress(heapData)))
        }
        solr.commit()
        def cpuDocsQuery = new QueryRequest(params((QT): '/select', (Q): "$METRIC:cpu"))
        def heapDocsQuery = new QueryRequest(params((QT): '/select', (Q): "$METRIC:heap"))

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): METRIC, (POINTS_PER_CHUNK): 100)))
        SolrDocumentList cpuDocs = solr.request(cpuDocsQuery).get('response')
        SolrDocumentList heapDocs = solr.request(heapDocsQuery).get('response')

        then:
        cpuDocs.size() == 1
        heapDocs.size() == 1
        decompress(cpuDocs[0].get(DATA), 1, 100).entrySet().each { assert sin(it.key) == it.value }
        decompress(heapDocs[0].get(DATA), 1, 100).entrySet().each { assert cos(it.key) == it.value }
    }
}