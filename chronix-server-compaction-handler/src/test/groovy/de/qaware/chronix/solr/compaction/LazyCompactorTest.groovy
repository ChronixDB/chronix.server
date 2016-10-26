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

import de.qaware.chronix.timeseries.dts.Point
import org.apache.lucene.document.*
import org.apache.solr.schema.*
import spock.lang.Specification

import static de.qaware.chronix.Schema.*
import static de.qaware.chronix.converter.common.Compression.compress
import static de.qaware.chronix.converter.common.MetricTSSchema.METRIC
import static de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer.to

/**
 * Test case for {@link LazyCompactor}.
 *
 * @author alex.christ
 */
class LazyCompactorTest extends Specification {
    LazyCompactor compactor
    IndexSchema schema

    def setup() {
        compactor = new LazyCompactor()
        schema = Mock()
        schema.getField(START) >> new SchemaField(START, new TrieDoubleField())
        schema.getField(END) >> new SchemaField(END, new TrieDoubleField())
        schema.getField(METRIC) >> new SchemaField(METRIC, new StrField())
        schema.getField(DATA) >> new SchemaField(DATA, new BinaryField())
    }

    def "test one document compaction"() {
        given:
        def data = compress(to([new Point(0, 1, 11), new Point(0, 2, 22)].iterator()))
        def doc = new Document().with {
            add(new LongPoint(START, 1))
            add(new LongPoint(END, 2))
            add(new StoredField(DATA, data))
            add(new StringField(METRIC, 'heap_usage', Field.Store.YES))
            (Document) it
        }

        when:
        def result = compactor.compact([doc], schema).toList()
        def originals = result[0].originalDocuments
        def compacted = result[0].resultingDocuments

        then:
        result.size() == 1
        originals.size() == 1
        compacted.size() == 1
        originals[0] == doc
        compacted[0][START].value == 1
        compacted[0][END].value == 2
        compacted[0][METRIC].value == 'heap_usage'
        compacted[0][DATA].value == data
    }
}
