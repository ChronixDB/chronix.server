/*
 * Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.response

import de.qaware.chronix.Schema
import de.qaware.chronix.converter.Compression
import de.qaware.chronix.serializer.JsonKassiopeiaSimpleSerializer
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.lucene.document.StoredField
import org.apache.solr.common.SolrDocument
import spock.lang.Specification

/**
 * Unit test for the Chronix transformer
 * @author f.lautnschlager
 */
class ChronixTransformerTest extends Specification {

    def "test create"() {
        given:

        def transformer = new ChronixTransformer()

        when:
        def docTransformer = transformer.create("dataAsJson", null, null)

        docTransformer.transform(doc, 0)

        then:
        doc.get(ChronixTransformer.DATA_AS_JSON) == expected

        where:
        doc << [docWithoutData(), docWitData()]
        expected << [null, "[[0],[1.0]]"]
    }

    def docWithoutData() {
        def doc = new SolrDocument()
        doc.addField(Schema.METRIC, "groovy")

        doc
    }

    def docWitData() {
        def doc = new SolrDocument()
        doc.addField(Schema.DATA, compressedJson())

        doc
    }

    StoredField compressedJson() {

        def ts = new MetricTimeSeries.Builder("groovy").point(0, 1).build()
        def ser = new JsonKassiopeiaSimpleSerializer()
        def jsonBytes = ser.toJson(ts.timestamps, ts.values);
        def compressed = Compression.compress(jsonBytes)

        new StoredField(Schema.DATA, compressed);
    }
}
