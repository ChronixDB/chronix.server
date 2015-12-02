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
package de.qaware.chronix.solr.client
import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.solr.test.converter.DefaultDocumentConverter
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import spock.lang.Specification
/**
 * Unit test for the ChronixSolrStorage class
 * @author f.lautenschlager
 */
class ChronixSolrStorageTest extends Specification {

    def "test stream"() {
        given:
        def converter = new DefaultDocumentConverter()
        def connection = Mock(SolrClient.class)
        def query = new SolrQuery("*:*")
        def storage = new ChronixSolrStorage<BinaryStorageDocument>()

        when:

        def stream = storage.stream(converter, connection, query, 0, 1, 200)

        then:
        stream != null
    }

    def "test add"() {
        given:
        def converter = new DefaultDocumentConverter()
        def connection = Mock(SolrClient.class)
        def timeSeries = []
        def storage = new ChronixSolrStorage<BinaryStorageDocument>()

        when:
        def result = storage.add(converter, timeSeries, connection)

        then:
        result
    }
}
