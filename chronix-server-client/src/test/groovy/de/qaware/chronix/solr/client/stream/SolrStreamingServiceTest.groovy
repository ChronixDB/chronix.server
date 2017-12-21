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
package de.qaware.chronix.solr.client.stream

import de.qaware.chronix.converter.BinaryTimeSeries
import de.qaware.chronix.solr.test.converter.DefaultTimeSeriesConverter
import de.qaware.chronix.solr.test.extensions.ReflectionHelper
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.StreamingResponseCallback
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.util.NamedList
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the  solr streaming service
 *
 * @author f.lautenschlager
 */
class SolrStreamingServiceTest extends Specification {

    /**
     * Test defaults
     */
    def converter = new DefaultTimeSeriesConverter();
    def solrQuery = new SolrQuery("*:*");

    @Unroll
    def "test hasNext() for #nrOfdocuments documents should be #result"() {
        given:
        def queryResponse = Mock(QueryResponse.class)
        def results = Mock(SolrDocumentList.class)
        def connection = Mock(SolrClient.class)

        def responseHeader = new NamedList<Object>()
        responseHeader.add("query_start_long", -1l)
        responseHeader.add("query_end_long", -1l)

        when:
        results.getNumFound() >> nrOfdocuments
        queryResponse.getResults() >> results
        queryResponse.getResponseHeader() >> responseHeader

        connection.queryAndStreamResponse(_ as SolrParams, _ as StreamingResponseCallback) >> queryResponse

        then:
        def streamingService = new SolrStreamingService<>(converter, solrQuery, connection, 200)
        streamingService.hasNext() == result

        where:
        nrOfdocuments << [0, 1, -1]
        result << [false, true, false]
    }

    def "test next()"() {
        given:
        def nrOfDocuments = 4
        def queryResponse = Mock(QueryResponse.class)
        def results = Mock(SolrDocumentList.class)
        def connection = Mock(SolrClient.class)
        def streamingService = new SolrStreamingService<>(converter, solrQuery, connection, 200)

        //pre fill handler
        streamingService.solrStreamingHandler.init(nrOfDocuments, 0)
        streamingService.solrStreamingHandler.streamDocListInfo(nrOfDocuments, 0, 0)
        for (int i = 0; i < nrOfDocuments; i++) {
            streamingService.solrStreamingHandler.streamSolrDocument(new SolrDocument())
        }

        def docCounter = 0;

        def responseHeader = new NamedList<Object>()
        responseHeader.add("query_start_long", -1l)
        responseHeader.add("query_end_long", -1l)

        when:
        results.getNumFound() >> nrOfDocuments
        queryResponse.getResults() >> results
        queryResponse.getResponseHeader() >> responseHeader
        connection.queryAndStreamResponse(_ as SolrParams, _ as StreamingResponseCallback) >> queryResponse

        streamingService.hasNext()

        //add four document to the queue
        streamingService.timeSeriesHandler.queue.addAll([new BinaryTimeSeries.Builder().id("1").data("1".bytes).build(),
                                                         new BinaryTimeSeries.Builder().id("2").data("2".bytes).build(),
                                                         new BinaryTimeSeries.Builder().id("3").data("3".bytes).build(),
                                                         new BinaryTimeSeries.Builder().id("4").data("4".bytes).build()])

        then:
        while (streamingService.hasNext()) {
            docCounter++
            streamingService.next() != null
        }
        nrOfDocuments == docCounter
    }

    def "test noSuchElementException"() {
        given:
        def connection = Mock(SolrClient.class)
        def streamingService = new SolrStreamingService<>(converter, solrQuery, connection, 2)
        streamingService.solrStreamingHandler.init(1, 0)

        when:
        streamingService.currentDocumentCount = 1
        streamingService.next()

        then:
        thrown NoSuchElementException
    }

    def "test hasNext when a solr server exception is thrown"() {
        given:
        def connection = Mock(SolrClient.class)
        def logger = Mock(Logger.class)

        when:
        connection.queryAndStreamResponse(_ as SolrParams, _ as StreamingResponseCallback) >> {
            throw new SolrServerException("Test exception")
        }
        def streamingService = new SolrStreamingService<>(converter, solrQuery, connection, 200)
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", streamingService)

        def result = streamingService.hasNext()

        then:
        !result
        1 * logger.error(_ as String, _ as Throwable)

    }
}
