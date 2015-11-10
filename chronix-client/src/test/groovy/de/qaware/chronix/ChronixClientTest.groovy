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
package de.qaware.chronix

import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.converter.DefaultDocumentConverter
import de.qaware.chronix.helper.ReflectionHelper
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrDocumentList
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

/**
 * Unit test for the Chronix client.
 *
 * @author f.lautenschlager
 */
class ChronixClientTest extends Specification {

    def converter = new DefaultDocumentConverter()

    @Unroll
    def "test add #documents to chronix"() {
        given:
        def connection = Mock(SolrClient.class)
        def chronix = new ChronixClient(converter)

        UpdateResponse response = Mock(UpdateResponse.class)
        response.getStatus() >> 0
        connection.add(_ as Collection, _ as Integer) >> response

        when:
        chronix.add(documents, connection)

        then:
        calls * connection.add(_ as Collection, _ as Integer)
        0 * connection.commit(_ as String)

        where:
        documents << [[new BinaryStorageDocument.Builder().build(), new BinaryStorageDocument.Builder().build()]]
        calls << [1]

    }

    def "test empty stream"() {
        given:
        def connection = Mock(SolrClient.class)
        def query = new SolrQuery("*:*");
        def start = Instant.now().toEpochMilli()
        def end = Instant.now().toEpochMilli()

        def queryResponse = Mock(QueryResponse.class);
        queryResponse.getResults() >> new SolrDocumentList()
        connection.query(_ as SolrQuery) >> queryResponse

        when:
        def streamResult = new ChronixClient(converter).stream(query, start, end, connection);

        then:
        streamResult != null
        streamResult.count() == 0
    }

    def "test add to chronix when an solr server exception is thrown"() {
        given:
        def connection = Mock(SolrClient.class)
        def chronix = new ChronixClient(converter)
        def logger = Mock(Logger.class)

        connection.add(_ as Collection, _ as Integer) >> { throw new SolrServerException("Test Exception") }

        when:
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", chronix)
        def result = chronix.add(new ArrayList(), connection)

        then:
        result == null
        0 * connection.commit(_)
        1 * logger.error(_ as String, _ as Throwable)
    }
}
