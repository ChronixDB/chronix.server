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

import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.solr.common.util.SimpleOrderedMap
import org.apache.solr.handler.component.PivotFacetProcessor
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.search.DocSet
import org.apache.solr.search.SolrIndexSearcher
import spock.lang.Specification

/**
 * Test case for {@link SolrFacetService}.
 *
 * @author alex.christ
 */
class SolrFacetServiceTest extends Specification {
    SolrFacetService service
    SolrQueryRequest req
    SolrQueryResponse rsp
    SolrIndexSearcher searcher
    PivotFacetProcessor processor
    SolrFacetService.DependencyProvider dependencyProvider

    def setup() {
        req = Mock()
        rsp = Mock()
        searcher = Mock()
        processor = Mock()
        dependencyProvider = Mock()
        req.getSearcher() >> searcher
        searcher.getDocSet(_) >> Mock(DocSet)
        dependencyProvider.pivotFacetProcessor(_, _, _, _) >> processor
        service = new SolrFacetService(req, rsp, dependencyProvider)
    }

    def "test pivots"() {
        when:
        service.pivot('metric,host', new MatchAllDocsQuery())

        then:
        1 * processor.process('metric,host') >> [:]
    }

    def "ToTimeSeriesIds"() {
        def pivotResult = [
                nl('field', 'metric', 'value', 'cpu', 'count', 1),
                nl('field', 'metric', 'value', 'heap', 'count', 1, 'pivot', [
                        nl('field', 'host', 'value', 'h01', 'count', 1),
                        nl('field', 'host', 'value', 'h02', 'count', 1)])]

        when:
        def ids = service.toTimeSeriesIds(pivotResult)

        then:
        ids.collect { it.toString() } == ['[metric:cpu]', '[host:h01,metric:heap]', '[host:h02,metric:heap]']
    }

    def nl(Object... keyValues) {
        def result = new SimpleOrderedMap<Object>()
        for (int i = 0; i < keyValues.length; i += 2) {
            result.add(keyValues[i], keyValues[i + 1])
        }
        result
    }
}