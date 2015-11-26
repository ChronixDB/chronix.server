/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.analysis.aggregation

import org.apache.lucene.search.IndexSearcher
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.handler.component.ResponseBuilder
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.search.AnalyticsQuery
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the aggregation plugin
 * @author f.lautenschlager
 */
class AggregationPluginTest extends Specification {

    def "test successful parser creation "() {
        given:
        def aggregation = new AggregationPlugin()

        def params = new ModifiableSolrParams()
        def request = Mock(SolrQueryRequest)

        def responseBuilder = Mock(ResponseBuilder)
        def indexReader = Mock(IndexSearcher)
        when:

        params.add("ag", "max")
        params.add("query_start_long", String.valueOf(start));
        params.add("query_end_long", String.valueOf(end));

        def parser = aggregation.createParser("", params, params, request)
        def query = (AnalyticsQuery) parser.parse()
        def aggregateCollector = (AggregationCollector) query.getAnalyticsCollector(responseBuilder, indexReader)

        then:
        aggregateCollector.queryStart == expectedStart
        aggregateCollector.queryEnd == expectedEnd

        where:
        start << [-1, 4711]
        end << [-1, 598747]

        expectedStart << [0, 4711]
        expectedEnd << [Long.MAX_VALUE, 598747l]
    }

    @Unroll
    def "test exception cases. Exceptions expected @exception for @params"() {
        given:
        def aggregation = new AggregationPlugin()

        def request = Mock(SolrQueryRequest)

        when:
        def exceptionThrown = false;
        try {
            aggregation.createParser("", params, params, request).parse()
        } catch (Exception e) {
            exceptionThrown = true;
        }

        then:
        exceptionThrown == exception

        where:
        params << [new ModifiableSolrParams()]
        exception << [true]

    }

    def "test init"() {
        given:
        def aggregation = new AggregationPlugin()

        when:
        aggregation.init(null)

        then:
        noExceptionThrown()

    }
}
