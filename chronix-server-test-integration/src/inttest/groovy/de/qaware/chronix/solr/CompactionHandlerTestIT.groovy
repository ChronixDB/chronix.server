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
package de.qaware.chronix.solr

import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.util.NamedList
import spock.lang.Shared
import spock.lang.Specification

import static de.qaware.chronix.Schema.DATA
import static de.qaware.chronix.Schema.END
import static de.qaware.chronix.solr.TestUtils.*
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.JOIN_KEY
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.POINTS_PER_CHUNK
import static java.lang.Math.cos
import static java.lang.Math.sin
import static org.apache.solr.common.params.CommonParams.*

/**
 * Integration test for {@link de.qaware.chronix.solr.compaction.ChronixCompactionHandler}.
 *
 * @author alex.christ
 */
class CompactionHandlerTestIT extends Specification {

    QueryRequest ALL_DOCS_QUERY = new QueryRequest(params((QT): '/select', (Q): '*:*'))
    @Shared
    HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8913/solr/chronix/").build()

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
        solr.add([doc((START): 1, (END): 2, (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(1L: 10d, 2L: 20d))])
        solr.commit()
        def compactionQuery = new QueryRequest(params((QT): '/compact', (JOIN_KEY): NAME, (POINTS_PER_CHUNK): 10))

        when:
        NamedList rsp = solr.request(compactionQuery).get('responseHeader')


        then:
        rsp.get('status') == 0
    }

    def "test compaction of a small number of documents"() {
        given:
        solr.add([doc((START): 1, (END): 2, (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(1L: 10d, 2L: 20d)),
                  doc((START): 3, (END): 4, (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(3L: 30d, 4L: 40d)),
                  doc((START): 5, (END): 6, (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(5L: 50d, 6L: 60d))])
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): NAME, (POINTS_PER_CHUNK): 10)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 1
        decompress(docs[0].get(DATA), 1, 6) == [1L: 10d, 2L: 20d, 3L: 30d, 4L: 40d, 5L: 50d, 6L: 60d]
    }

    def "test compaction of a large number of documents"() {
        given:
        (1L..100L).collate(5).each { range ->
            def data = range.collectEntries { it -> [it, sin(it)] }
            solr.add(doc((START): range.first(), (END): range.last(), (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(data)))
        }
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): NAME, (POINTS_PER_CHUNK): 100)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 1
        decompress(docs[0].get(DATA), 1, 100).entrySet().each { assert sin(it.key) == it.value }
    }

    def "test special chars as value of a join key field"() {
        given:
        solr.add([doc((START): 5, (END): 6, (NAME): 'a:AND() {!$#}\\', (TYPE): 'metric', (DATA): compress(1L: 10d))])
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): NAME, (POINTS_PER_CHUNK): 10)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 1
        docs[0].get(NAME) == 'a:AND() {!$#}\\'
        decompress(docs[0].get(DATA), 1, 6) == [1L: 10d]
    }

    def "test widow handdling"() {
        given:
        (1L..10L).collate(5).each { range ->
            def data = range.collectEntries { it -> [it, sin(it)] }
            solr.add(doc((START): range.first(), (END): range.last(), (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(data)))
        }
        solr.commit()

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): NAME, (POINTS_PER_CHUNK): 4)))
        SolrDocumentList docs = solr.request(ALL_DOCS_QUERY).get('response')

        then:
        docs.size() == 3
    }

    def "test compaction of two time series"() {
        given:
        (1L..100L).collate(5).each { def range ->
            def cpuData = range.collectEntries { it -> [it, sin(it)] }
            def heapData = range.collectEntries { it -> [it, cos(it)] }
            solr.add(doc((START): range.first(), (END): range.last(), (NAME): 'cpu', (TYPE): 'metric', (DATA): compress(cpuData)))
            solr.add(doc((START): range.first(), (END): range.last(), (NAME): 'heap', (TYPE): 'metric', (DATA): compress(heapData)))
        }
        solr.commit()
        def cpuDocsQuery = new QueryRequest(params((QT): '/select', (Q): "$NAME:cpu"))
        def heapDocsQuery = new QueryRequest(params((QT): '/select', (Q): "$NAME:heap"))

        when:
        solr.request(new QueryRequest(params((QT): '/compact', (JOIN_KEY): NAME, (POINTS_PER_CHUNK): 100)))
        SolrDocumentList cpuDocs = solr.request(cpuDocsQuery).get('response')
        SolrDocumentList heapDocs = solr.request(heapDocsQuery).get('response')

        then:
        cpuDocs.size() == 1
        heapDocs.size() == 1
        decompress(cpuDocs[0].get(DATA), 1, 100).entrySet().each { assert sin(it.key) == it.value }
        decompress(heapDocs[0].get(DATA), 1, 100).entrySet().each { assert cos(it.key) == it.value }
    }

    def "test fq"() {
        given:
        solr.add([doc((START): 1, (END): 2, (NAME): 'cpu', host: '1', (TYPE): 'metric', (DATA): compress(1L: 10d, 2L: 20d)),
                  doc((START): 3, (END): 4, (NAME): 'cpu', host: '1', (TYPE): 'metric', (DATA): compress(3L: 30d, 4L: 40d)),
                  doc((START): 5, (END): 6, (NAME): 'cpu', host: '2', (TYPE): 'metric', (DATA): compress(5L: 50d, 6L: 60d))])
        solr.commit()
        def compactionQuery = new QueryRequest(params((QT): '/compact', (FQ): 'host:1'))
        def h1Query = new QueryRequest(params((QT): '/select', (Q): 'host:1'))
        def h2Query = new QueryRequest(params((QT): '/select', (Q): 'host:2'))

        when:
        solr.request(compactionQuery).get('responseHeader')
        SolrDocumentList h1Docs = solr.request(h1Query).get('response')
        SolrDocumentList h2Docs = solr.request(h2Query).get('response')

        then:
        h1Docs.size() == 1
        h2Docs.size() == 1
        decompress(h1Docs[0].get(DATA), 1, 4) == [1L: 10d, 2L: 20d, 3L: 30d, 4L: 40d]
        decompress(h2Docs[0].get(DATA), 5, 6) == [5L: 50d, 6L: 60d]
    }

    def "test fq with joinKey"() {
        given:
        solr.add([doc((START): 10, (END): 11, (NAME): 'cpu', host: '1', (TYPE): 'metric', (DATA): compress(10L: 10d, 11L: 11d)),
                  doc((START): 12, (END): 13, (NAME): 'cpu', host: '1', (TYPE): 'metric', (DATA): compress(12L: 12d, 13L: 13d)),
                  doc((START): 14, (END): 15, (NAME): 'cpu', host: '2', (TYPE): 'metric', (DATA): compress(14L: 14d, 15L: 15d)),
                  doc((START): 16, (END): 17, (NAME): 'mem', host: '1', (TYPE): 'metric', (DATA): compress(16L: 16d, 17L: 17d)),
                  doc((START): 18, (END): 19, (NAME): 'mem', host: '1', (TYPE): 'metric', (DATA): compress(18L: 18d, 19L: 19d)),
                  doc((START): 20, (END): 21, (NAME): 'mem', host: '2', (TYPE): 'metric', (DATA): compress(20L: 20d, 21L: 21d))])
        solr.commit()
        def compactionQuery = new QueryRequest(params((QT): '/compact', (FQ): 'host:1', (JOIN_KEY): NAME))
        def h1CpuQuery = new QueryRequest(params((QT): '/select', (Q): "host:1 AND $NAME:cpu"))
        def h2CpuQuery = new QueryRequest(params((QT): '/select', (Q): "host:2 AND $NAME:cpu"))
        def h1MemQuery = new QueryRequest(params((QT): '/select', (Q): "host:1 AND $NAME:mem"))
        def h2MemQuery = new QueryRequest(params((QT): '/select', (Q): "host:2 AND $NAME:mem"))

        when:
        solr.request(compactionQuery).get('responseHeader')
        SolrDocumentList h1CpuDocs = solr.request(h1CpuQuery).get('response')
        SolrDocumentList h2CpuDocs = solr.request(h2CpuQuery).get('response')
        SolrDocumentList h1MemDocs = solr.request(h1MemQuery).get('response')
        SolrDocumentList h2MemDocs = solr.request(h2MemQuery).get('response')

        then:
        h1CpuDocs.size() == 1
        h2CpuDocs.size() == 1
        h1MemDocs.size() == 1
        h2MemDocs.size() == 1
        decompress(h1CpuDocs[0].get(DATA), 10, 13) == [10L: 10d, 11L: 11d, 12L: 12d, 13L: 13d]
        decompress(h2CpuDocs[0].get(DATA), 14, 15) == [14L: 14d, 15L: 15d]
        decompress(h1MemDocs[0].get(DATA), 16, 19) == [16L: 16d, 17L: 17d, 18L: 18d, 19L: 19d]
        decompress(h2MemDocs[0].get(DATA), 20, 21) == [20L: 20d, 21L: 21d]
    }
}