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

import org.apache.solr.schema.*
import spock.lang.Specification

import static de.qaware.chronix.Schema.*
import static de.qaware.chronix.converter.common.MetricTSSchema.METRIC
import static de.qaware.chronix.solr.compaction.TestUtils.*

/**
 * Test case for {@link LazyCompactor}.
 *
 * @author alex.christ
 */
class LazyCompactorTest extends Specification {
    IndexSchema schema

    def setup() {
        schema = Mock()
        schema.getField(START) >> new SchemaField(START, new TrieDoubleField())
        schema.getField(END) >> new SchemaField(END, new TrieDoubleField())
        schema.getField(METRIC) >> new SchemaField(METRIC, new StrField())
        schema.getField(DATA) >> new SchemaField(DATA, new BinaryField())
    }

    def "test one document compaction"() {
        given:
        def doc = doc 'load_avg', [1: 10, 2: 20]

        when:
        def result = new LazyCompactor().compact([doc], schema).toList()
        def outDoc1 = result[0].outputDocuments[0]

        then:
        result.size() == 1
        result[0].inputDocuments == [doc] as Set
        result[0].outputDocuments.size() == 1
        outDoc1 hasAttributes((START): 1, (END): 2, (METRIC): 'load_avg', (DATA): compress(1: 10, 2: 20))
    }

    def "test compact 2 documents into 1"() {
        given:
        def doc1 = doc 'load_avg', [1: 10, 2: 20]
        def doc2 = doc 'load_avg', [3: 30, 4: 40]

        when:
        def result = new LazyCompactor().compact([doc1, doc2], schema).toList()
        def outDoc1 = result[0].outputDocuments[0]

        then:
        result.size() == 1
        result[0].inputDocuments == [doc1, doc2] as Set
        result[0].outputDocuments.size() == 1

        outDoc1 hasAttributes((START): 1, (END): 4, (METRIC): 'load_avg', (DATA): compress(1: 10, 2: 20, 3: 30, 4: 40))
    }

    def "test 3 documents compacted into 2"() {
        given:
        def doc1 = doc 'load_avg', [1: 10, 2: 20]
        def doc2 = doc 'load_avg', [3: 30, 4: 40]
        def doc3 = doc 'load_avg', [5: 50, 6: 60]

        when:
        def result = new LazyCompactor(2).compact([doc1, doc2, doc3], schema).toList()
        def outDoc1 = result[0].outputDocuments[0]
        def outDoc2 = result[1].outputDocuments[0]

        then:
        result.size() == 2
        result[0].inputDocuments == [doc1, doc2] as Set
        result[1].inputDocuments == [doc3] as Set

        outDoc1 hasAttributes((START): 1, (END): 4, (METRIC): 'load_avg', (DATA): compress(1: 10, 2: 20, 3: 30, 4: 40))
        outDoc2 hasAttributes((START): 5, (END): 6, (METRIC): 'load_avg', (DATA): compress(5: 50, 6: 60))
    }
}