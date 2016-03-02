/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.response

import de.qaware.chronix.Schema
import de.qaware.chronix.converter.common.MetricTSSchema
import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StoredField
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.params.SolrParams
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
        def params = Stub(SolrParams)
        params.get("paa", _ as String) >> "4"
        params.get("alpha", _ as String) >> "7"
        params.get("threshold", _ as String) >> "0.01"
        when:
        def docTransformer = transformer.create("dataAsJson", params, null)

        docTransformer.transform(doc, 0)

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

    def "test create as SAX"() {
        given:
        def transformer = new ChronixTransformer()
        def params = Stub(SolrParams)
        params.get("paa", _ as String) >> "4"
        params.get("alpha", _ as String) >> "7"
        params.get("threshold", _ as String) >> "0.01"
        when:
        def docTransformer = transformer.create("dataAsSAX", params, null)

        docTransformer.transform(doc, 0)

        then:
        doc.get(ChronixTransformer.DATA_AS_SAX) == expected

        where:
        doc << [docWithoutData(), docWitData()]
        expected << [null, "aceg"]
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
        doc.addField(Schema.END, new LongField(Schema.END, start.plusSeconds(3).toEpochMilli(), Field.Store.YES))

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
