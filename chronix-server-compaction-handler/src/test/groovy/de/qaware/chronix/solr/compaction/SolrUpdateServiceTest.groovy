/*
 * Copyright (C) 2016 QAware GmbH
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

import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.update.processor.UpdateRequestProcessor
import spock.lang.Specification

import static de.qaware.chronix.Schema.ID

/**
 * Test case for {@link SolrUpdateService}.
 * @author alex.christ
 */
class SolrUpdateServiceTest extends Specification {
    SolrUpdateService service
    UpdateRequestProcessor updateProcessor
    SolrQueryRequest req

    void setup() {
        updateProcessor = Mock(UpdateRequestProcessor)
        req = Mock()
        service = new SolrUpdateService(req, updateProcessor)
    }

    def "test deleting"() {
        given:
        def doc = new Document().with {
            add(new StringField(ID, 'some-id', Field.Store.YES))
            (Document) it
        };

        when:
        service.delete doc

        then:
        1 * updateProcessor.processDelete({ it.query == "$ID:some-id" })
    }

    def "test adding"() {
        given:
        def doc = new SolrInputDocument()

        when:
        service.add doc

        then:
        1 * updateProcessor.processAdd({ it.solrInputDocument == doc })

    }

    def "test committing"() {
        when:
        service.commit();

        then:
        1 * updateProcessor.processCommit({ it.req == req })
    }
}
