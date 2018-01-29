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
package de.qaware.chronix.solr.client

import de.qaware.chronix.converter.BinaryTimeSeries
import de.qaware.chronix.solr.test.converter.DefaultTimeSeriesConverter
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.StreamingResponseCallback
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.util.NamedList
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.BinaryOperator
import java.util.function.Function
/**
 * Unit test for the ChronixSolrStorage class
 * @author f.lautenschlager
 */
class ChronixSolrStorageTest extends Specification {

    @Shared
    Function<BinaryTimeSeries, String> groupBy = new Function<BinaryTimeSeries, String>() {
        @Override
        String apply(BinaryTimeSeries binaryTimeSeries) {
            return binaryTimeSeries.id;
        }
    }

    @Shared
    BinaryOperator<BinaryTimeSeries> reduce = new BinaryOperator<BinaryTimeSeries>() {
        @Override
        BinaryTimeSeries apply(BinaryTimeSeries binaryTimeSeries, BinaryTimeSeries binaryTimeSeries2) {
            return binaryTimeSeries;
        }
    }

    def "test stream"() {
        given:
        def converter = new DefaultTimeSeriesConverter()
        def connection = Mock(SolrClient.class)
        def query = new SolrQuery("*:*")
        def storage = new ChronixSolrStorage<BinaryTimeSeries>(200, groupBy, reduce)

        def response = Mock(QueryResponse)
        def results = new SolrDocumentList()
        def responseHeader = new NamedList<Object>()
        responseHeader.add(ChronixSolrStorageConstants.QUERY_START_LONG, 0l)
        responseHeader.add(ChronixSolrStorageConstants.QUERY_END_LONG, Long.MAX_VALUE)

        results.setNumFound(0)
        response.getResults() >> results
        response.getResponseHeader() >> responseHeader

        connection.queryAndStreamResponse(_ as SolrQuery, _ as StreamingResponseCallback) >> response

        when:
        def stream = storage.stream(converter, connection, query)

        then:
        stream != null
        stream.count() == 0
    }

    def "test add"() {
        given:
        def converter = new DefaultTimeSeriesConverter()
        def connection = Mock(SolrClient.class)
        def timeSeries = []
        def storage = new ChronixSolrStorage<BinaryTimeSeries>(200, groupBy, reduce)

        when:
        def result = storage.add(converter, timeSeries, connection)

        then:
        result
    }
}
