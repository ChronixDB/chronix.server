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
package de.qaware.chronix.solr.response

import de.qaware.chronix.Schema
import de.qaware.chronix.converter.common.MetricTSSchema
import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.StoredField
import org.apache.solr.common.SolrDocument
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

/**
 * Unit test for the Chronix transformer
 * @author f.lautnschlager
 */
class ChronixTransformerTest extends Specification {

    @Shared
    def start = Instant.parse("2016-01-06T07:47:15.416Z")


    def "test create"() {
        given:

        def transformer = new ChronixTransformer()

        when:
        def docTransformer = transformer.create("dataAsJson", null, null)

        docTransformer.transform(doc, 0, 0.0f)

        then:
        doc.get(ChronixTransformer.DATA_AS_JSON) == expected

        where:
        doc << [docWithoutData(), docWitData()]
        expected << [null, "[[${start.toEpochMilli()}," +
                "${start.plusSeconds(1).toEpochMilli()}," +
                "${start.plusSeconds(2).toEpochMilli()}," +
                "${start.plusSeconds(3).toEpochMilli()}]" +
                ",[1.0,2.0,3.0,4.0]]"]
    }

    def docWithoutData() {
        def doc = new SolrDocument()
        doc.addField(MetricTSSchema.METRIC, "groovy")

        doc
    }

    def docWitData() {
        def doc = new SolrDocument()

        doc.addField(Schema.DATA, compressedProtoBuf())
        doc.addField(Schema.START, new StoredField(Schema.START, start.toEpochMilli()))
        doc.addField(Schema.END, new LongPoint(Schema.END, start.plusSeconds(3).toEpochMilli()))

        doc
    }

    StoredField compressedProtoBuf() {

        def ts = new MetricTimeSeries.Builder("groovy")
                .point(start.toEpochMilli(), 1)
                .point(start.plusSeconds(1).toEpochMilli(), 2)
                .point(start.plusSeconds(2).toEpochMilli(), 3)
                .point(start.plusSeconds(3).toEpochMilli(), 4)
                .build()

        def bytes = ProtoBufKassiopeiaSimpleSerializer.to(ts.points().iterator())
        new StoredField(Schema.DATA, bytes);
    }
}
