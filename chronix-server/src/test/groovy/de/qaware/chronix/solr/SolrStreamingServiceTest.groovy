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
package de.qaware.chronix.solr

import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.converter.DefaultDocumentConverter
import de.qaware.chronix.helper.ReflectionHelper
import de.qaware.chronix.solr.stream.SolrStreamingService
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.params.SolrParams
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

/**
 * Unit test for the  solr streaming service
 *
 * @author f.lautenschlager
 */
class SolrStreamingServiceTest extends Specification {

    /**
     * Test defaults
     */
    def converter = new DefaultDocumentConverter();
    def solrQuery = new SolrQuery("*:*")
    def dateIgnored = Instant.now().toEpochMilli()


    @Unroll
    def "test hasNext() for #nrOfdocuments documents should be #result"() {
        given:
        def queryResponse = Mock(QueryResponse.class)
        def results = Mock(SolrDocumentList.class)
        def connection = Mock(SolrClient.class)

        when:
        results.getNumFound() >> nrOfdocuments
        queryResponse.getResults() >> results
        connection.query(_ as SolrParams) >> queryResponse

        then:
        def streamingService = new SolrStreamingService<>(converter, solrQuery, dateIgnored, dateIgnored, connection, 200)
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
        def streamingService = new SolrStreamingService<>(converter, solrQuery, dateIgnored, dateIgnored, connection, nrOfDocuments)

        //pre fill handler
        streamingService.solrStreamingHandler.init(nrOfDocuments, 0)
        streamingService.solrStreamingHandler.streamDocListInfo(nrOfDocuments, 0, 0)
        for (int i = 0; i < nrOfDocuments; i++) {
            streamingService.solrStreamingHandler.streamSolrDocument(new SolrDocument())
        }

        def docCounter = 0;

        when:
        results.getNumFound() >> nrOfDocuments
        queryResponse.getResults() >> results
        connection.query(_ as SolrParams) >> queryResponse

        streamingService.hasNext()

        //add four document to the queue
        streamingService.timeSeriesHandler.queue.addAll([new BinaryStorageDocument.Builder().id("1").data("1".bytes).build(),
                                                         new BinaryStorageDocument.Builder().id("2").data("2".bytes).build(),
                                                         new BinaryStorageDocument.Builder().id("3").data("3".bytes).build(),
                                                         new BinaryStorageDocument.Builder().id("4").data("4".bytes).build()])

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
        def streamingService = new SolrStreamingService<>(converter, solrQuery, dateIgnored, dateIgnored, connection, 2)

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
        connection.query(_ as SolrParams) >> { throw new SolrServerException("Test exception") }
        def streamingService = new SolrStreamingService<>(converter, solrQuery, dateIgnored, dateIgnored, connection, 200)
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", streamingService)

        def result = streamingService.hasNext()

        then:
        !result
        1 * logger.error(_ as String, _ as Throwable)

    }
}
