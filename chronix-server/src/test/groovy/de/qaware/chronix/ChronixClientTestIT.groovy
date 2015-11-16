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
import de.qaware.chronix.solr.ChronixSolrStorage
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.core.CoreContainer
import spock.lang.Specification

import java.time.Instant
import java.util.stream.Collectors

/**
 * Tests the integration of Chronix and an embedded solr.
 * Fields also have to be registered in the schema.xml
 * (\src\test\resources\de\qaware\chronix\chronix\conf\schema.xml)
 *
 * @author f.lautenschlager
 */
class ChronixClientTestIT extends Specification {

    def setup() {
        given:
        def embeddedClient = createAnEmbeddedSolrClient()

        when:
        def result = embeddedClient.ping()
        embeddedClient.deleteByQuery("*:*")
        embeddedClient.commit()

        then:
        result.status == 0
        embeddedClient.close()
    }


    def "Test add and query documents to Chronix with embedded Solr"() {
        given:
        SolrClient embeddedClient = createAnEmbeddedSolrClient()
        def storage = new ChronixSolrStorage()
        def chronix = new ChronixClient(new DefaultDocumentConverter(), storage)
        def start = Instant.now().toEpochMilli()
        def end = Instant.now().plusSeconds(64000).toEpochMilli()

        def listStringField = ["List first part", "List second part"]
        def listIntField = [1I, 2I]
        def listLongField = [11L, 25L]
        def listDoubleField = [1.5D, 2.6D]

        when:
        //add some documents
        def documents = new ArrayList()
        10.times {
            //id is set by solr, we have no custom field registered in the solr schema.xml
            def builder = new BinaryStorageDocument.Builder()
                    .start(start)
                    .end(end)
                    .data("You can put in, whatever you want".getBytes("UTF-8"))
                    .field("myIntField", 5)
                    .field("myLongField", 8L)
                    .field("myDoubleField", 5.5)
                    .field("myByteField", "String as byte".getBytes("UTF-8"))
                    .field("myStringList", listStringField)
                    .field("myIntList", listIntField)
                    .field("myLongList", listLongField)
                    .field("myDoubleList", listDoubleField)

            documents.add(builder.build())
        }

        chronix.add(documents, embeddedClient)

        //we do a hart commit - only for testing purposes
        embeddedClient.commit()

        //query all documents
        List<BinaryStorageDocument> documentCount = chronix.stream(embeddedClient, new SolrQuery("*:*"), start, end, 200).collect(Collectors.toList());

        then:
        documentCount.size() == 10i
        def queriedDocument = documentCount.get(0)

        queriedDocument.start == start
        queriedDocument.end == end
        queriedDocument.data == "You can put in, whatever you want".getBytes("UTF-8")
        queriedDocument.get("myIntField") == 5
        queriedDocument.get("myLongField") == 8L
        queriedDocument.get("myDoubleField") == 5.5D
        queriedDocument.get("myByteField") == "String as byte".getBytes("UTF-8")
        queriedDocument.get("myStringList") == listStringField
        queriedDocument.get("myIntList") == listIntField
        queriedDocument.get("myLongList") == listLongField
        queriedDocument.get("myDoubleList") == listDoubleField

    }

    def createAnEmbeddedSolrClient() {
        System.setProperty("solr.data.dir", "${new File("build").absolutePath}/data");

        File solrXml = new File("src/test/resources/de/qaware/chronix/solr.xml")
        CoreContainer solrContainer = CoreContainer.createAndLoad(solrXml.getParent(), solrXml)
        return new EmbeddedSolrServer(solrContainer, "chronix")
    }

}
