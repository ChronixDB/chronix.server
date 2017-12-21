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
package de.qaware.chronix.solr.compaction

import org.apache.lucene.document.Document
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.search.QParser
import org.apache.solr.search.SolrIndexSearcher
import spock.lang.Specification

import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.*

/**
 * Test case for {@link ChronixCompactionHandler}.
 *
 * @author alex.christ
 */
class ChronixCompactionHandlerTest extends Specification {
    ChronixCompactionHandler handler
    SolrUpdateService updateService
    SolrFacetService facetService
    QParser parser
    LazyCompactor compactor
    LazyDocumentLoader documentLoader
    SolrQueryRequest req
    SolrQueryResponse rsp
    ModifiableSolrParams params
    ChronixCompactionHandler.DependencyProvider dependencyProvider

    def setup() {
        req = Mock()
        rsp = Mock()
        params = new ModifiableSolrParams([:])
        updateService = Mock()
        facetService = Mock()
        parser = Mock()
        compactor = Mock()
        documentLoader = Mock()
        req.getSearcher() >> Mock(SolrIndexSearcher)
        req.getParams() >> params
        dependencyProvider = Mock(ChronixCompactionHandler.DependencyProvider) {
            solrFacetService(*_) >> facetService
            solrUpdateService(*_) >> updateService
            parser(*_) >> Mock(QParser)
        }
        def dependencyProvider = dependencyProvider
        handler = new ChronixCompactionHandler(dependencyProvider)
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
        facetService.toTimeSeriesIds(_) >> [new TimeSeriesId([metric: 'cpu'])]
        def inputDocs = [new Document()] as Set
        def outputDocs = [new SolrInputDocument()] as Set
        compactor.compact(_) >> [new CompactionResult(inputDocs, outputDocs)]
        params.add(JOIN_KEY, 'metric,host')

        when:
        handler.handleRequestBody(req, rsp)

        then:
        1 * rsp.add('timeseries [metric:cpu] oldNumDocs:', 1)
        1 * rsp.add('timeseries [metric:cpu] newNumDocs:', 1)
        1 * updateService.delete([inputDocs[0]])
        1 * updateService.add([outputDocs[0]])
        1 * dependencyProvider.documentLoader(100, _) >> documentLoader
        1 * dependencyProvider.compactor(10000, _) >> compactor
    }

    def "test parameters"() {
        given:
        facetService.toTimeSeriesIds(_) >> [new TimeSeriesId([:])]
        compactor.compact(_) >> [new CompactionResult([] as Set, [] as Set)]
        params.add(JOIN_KEY, 'metric,host')
        params.add(PAGE_SIZE, '112')
        params.add(POINTS_PER_CHUNK, '327')

        when:
        handler.handleRequestBody(req, rsp)

        then:
        1 * facetService.pivot('metric,host', _)
        1 * dependencyProvider.documentLoader(112, _) >> documentLoader
        1 * dependencyProvider.compactor(327, _) >> compactor
    }
}