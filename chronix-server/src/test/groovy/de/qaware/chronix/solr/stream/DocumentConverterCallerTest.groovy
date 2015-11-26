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
package de.qaware.chronix.solr.stream

import de.qaware.chronix.Schema
import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.converter.DefaultDocumentConverter
import org.apache.lucene.document.StoredField
import org.apache.solr.common.SolrDocument
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit test for the document converter
 * @author f.lautenschlager
 */
class DocumentConverterCallerTest extends Specification {

    def "test call convert document for an embedded solr"() {
        given:
        def start = Instant.now().toEpochMilli()
        def end = Instant.now().plusSeconds(180).toEpochMilli()

        //Create a solr document
        def solrDocument = new SolrDocument()
        solrDocument.addField(Schema.START, new StoredField(Schema.START, start))
        solrDocument.addField(Schema.END, new StoredField(Schema.END, end))
        solrDocument.addField(Schema.DATA, new StoredField(Schema.DATA, "someBytes".bytes))
        solrDocument.addField("SomeField", new StoredField("SomeField", ChronoUnit.SECONDS.toString()));

        def converter = new DocumentConverterCaller(solrDocument, new DefaultDocumentConverter(), start, end)

        when:
        BinaryStorageDocument ts = converter.call()

        then:
        ts.get(Schema.START) == start
        ts.get(Schema.END) == end
        ts.get(Schema.DATA) == "someBytes".bytes
        ts.get("SomeField") == ChronoUnit.SECONDS.toString()
    }

    def "test call convert document for an remote solr"() {
        given:
        def start = Instant.now().toEpochMilli()
        def end = Instant.now().plusSeconds(180).toEpochMilli()

        //Create a solr document
        def solrDocument = new SolrDocument()
        solrDocument.addField(Schema.START, start)
        solrDocument.addField(Schema.END, end)
        solrDocument.addField(Schema.DATA, "someBytes".bytes)
        solrDocument.addField("SomeField", ChronoUnit.SECONDS.toString());

        def converter = new DocumentConverterCaller(solrDocument, new DefaultDocumentConverter(), start, end)

        when:
        BinaryStorageDocument ts = converter.call()

        then:
        ts.get(Schema.START) == start
        ts.get(Schema.END) == end
        ts.get(Schema.DATA) == "someBytes".bytes
        ts.get("SomeField") == ChronoUnit.SECONDS.toString()
    }

}
