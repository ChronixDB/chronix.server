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

import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.lucene.document.Document
import org.apache.lucene.document.DoublePoint
import org.apache.solr.common.SolrDocument
import org.apache.solr.schema.IndexSchema
import org.apache.solr.schema.SchemaField
import org.apache.solr.schema.TrieDoubleField
import spock.lang.Specification

import static de.qaware.chronix.Schema.ID
import static de.qaware.chronix.converter.common.MetricTSSchema.METRIC
import static de.qaware.chronix.solr.compaction.TestUtils.*

/**
 * Test case for {@link ConverterService}.
 *
 * @author alex.christ
 */
class ConverterServiceTest extends Specification {
    ConverterService service

    def setup() {
        service = new ConverterService()
    }

    def "convert timeseries to solr input document"() {
        given:
        def ts = new MetricTimeSeries.Builder('heap_usage').attributes([
                attr1    : 0,
                attr2    : 'hello',
                _version_: 1549200241530503168L,
                (ID)     : 'b515202e-d3fb-4d1c-878f-85bb45f89a69']).build()

        when:
        def result = service.toInputDocument(ts)

        then:
        result hasAttributes((METRIC): 'heap_usage', attr1: 0, attr2: 'hello')
        result['_version_'] == null
        result[ID] != 'b515202e-d3fb-4d1c-878f-85bb45f89a69'
    }

    def "convert solr document to time series"() {
        given:
        def doc = new SolrDocument().with {
            setField('start', 1L)
            setField('end', 4L)
            setField('host', 'some-host')
            setField('id', 'b515202e-d3fb-4d1c-878f-85bb45f89a69')
            setField('data', compress([1: 11, 2: 12, 4: 14]))
            setField('metric', 'heap_average')
            (SolrDocument) it
        }

        when:
        def result = service.toTimeSeries(doc)

        then:
        result timeseriesHasAttributes(metric: 'heap_average', start: 1, end: 4, valuesAsArray: [11, 12, 14])
        result.attribute('host') == 'some-host'
    }

    def "convert lucene document to solr document"() {
        given:
        def doc = new Document().with {
            add(new DoublePoint('doubleField', 1))
            (Document) it
        };
        def schema = Mock(IndexSchema)
        schema.getField('doubleField') >> new SchemaField('doubleField', new TrieDoubleField())

        when:
        def result = service.toSolrDoc(schema, doc)

        then:
        result['doubleField'] == 1
    }
}