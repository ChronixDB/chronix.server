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
package de.qaware.chronix.solr.client.add

import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.solr.test.converter.DefaultDocumentConverter
import de.qaware.chronix.solr.test.extensions.ReflectionHelper
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.response.UpdateResponse
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the solr adding service
 * @author f.lautenschlager
 */
class SolrAddingServiceTest extends Specification {


    @Unroll
    def "test add #documents to chronix"() {
        given:
        def converter = new DefaultDocumentConverter()
        def connection = Mock(SolrClient.class)

        UpdateResponse response = Mock(UpdateResponse.class)
        response.getStatus() >> 0

        when:
        connection.add(_ as Collection) >> response
        def result = SolrAddingService.add(converter, documents, connection)

        then:
        result
        0 * connection.commit(_ as String)

        where:
        documents << [[new BinaryStorageDocument.Builder().build(), new BinaryStorageDocument.Builder().build()], [], null]
        calls << [1, 0, 0]
    }


    def "test add to chronix when an solr server exception is thrown"() {
        given:
        def connection = Mock(SolrClient.class)
        def converter = new DefaultDocumentConverter()
        def timeSeries = [new BinaryStorageDocument.Builder().build()]

        def addingService = new SolrAddingService()
        def logger = Mock(Logger.class)
        ReflectionHelper.setValueToFieldOfObject(logger, "LOGGER", addingService)

        when:
        connection.add(_ as Collection) >> { throw new SolrServerException("Test Exception") }
        def result = addingService.add(converter, timeSeries, connection)

        then:
        !result
        0 * connection.commit(_ as String)
        1 * logger.error(_ as String, _ as Throwable)

    }


    def "test solr returns non zero status"() {
        given:
        def converter = new DefaultDocumentConverter()
        def timeSeries = [new BinaryStorageDocument.Builder().build(), new BinaryStorageDocument.Builder().build()]
        def connection = Mock(SolrClient.class)

        when:
        UpdateResponse response = Mock(UpdateResponse.class)
        response.getStatus() >> 1
        connection.add(_ as Collection) >> response

        def result = SolrAddingService.add(converter, timeSeries, connection)

        then:
        !result
        0 * connection.commit(_ as String)
    }

}