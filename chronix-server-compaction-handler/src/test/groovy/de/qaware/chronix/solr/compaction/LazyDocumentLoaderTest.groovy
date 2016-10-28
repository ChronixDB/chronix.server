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
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.Sort
import spock.lang.Specification

import static de.qaware.chronix.solr.compaction.TestUtils.asTopDocs

/**
 * @author alex.christ
 */
class LazyDocumentLoaderTest extends Specification {
    IndexSearcher searcher
    Query query
    Sort sort

    def setup() {
        searcher = Mock()
        sort = Mock()
    }

    def "test simple paging"() {
        given:
        def (sdoc1, sdoc2, sdoc3) = [new ScoreDoc(1, 0), new ScoreDoc(2, 0), new ScoreDoc(3, 0)]
        searcher.searchAfter(null, _, _, _) >> asTopDocs([sdoc1])
        searcher.searchAfter(sdoc1, _, _, _) >> asTopDocs([sdoc2])
        searcher.searchAfter(sdoc2, _, _, _) >> asTopDocs([sdoc3])
        searcher.searchAfter(sdoc3, _, _, _) >> asTopDocs([])

        def (doc1, doc2, doc3) = [new Document(), new Document(), new Document()]
        searcher.doc(1) >> doc1
        searcher.doc(2) >> doc2
        searcher.doc(3) >> doc3

        when:
        def docs = new LazyDocumentLoader().load(searcher, query, sort)

        then:
        docs.toList() == [doc1, doc2, doc3]

    }
}