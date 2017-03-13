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
package de.qaware.chronix.solr.type.metric

import de.qaware.chronix.converter.BinaryTimeSeries
import de.qaware.chronix.converter.MetricTimeSeriesConverter
import de.qaware.chronix.converter.TimeSeriesConverter
import de.qaware.chronix.converter.common.DoubleList
import de.qaware.chronix.converter.common.LongList
import de.qaware.chronix.server.functions.FunctionValueMap
import de.qaware.chronix.solr.type.metric.functions.aggregations.Percentile
import de.qaware.chronix.solr.type.metric.functions.analyses.Frequency
import de.qaware.chronix.solr.type.metric.functions.analyses.Trend
import de.qaware.chronix.solr.type.metric.functions.transformation.Derivative
import de.qaware.chronix.solr.type.metric.functions.transformation.Top
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.common.SolrDocument
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer

/**
 * @author f.lautenschlager
 */
class SolrDocumentBuilderTest extends Specification {

    def "test private constructor"() {
        when:
        SolrDocumentBuilder.newInstance()
        then:
        noExceptionThrown()
    }

    @Unroll
    def "test reduce to time series with data: #withData"() {

        given:
        def solrDocuments = fillDocs()

        when:
        def ts = SolrDocumentBuilder.reduceDocumentToTimeSeries(0l, 100l, solrDocuments, true)

        then:
        if (withData) {
            ts.sort()
            ts.size() == 70
            ts.getEnd() == 100
            ts.getStart() == 1
        } else {
            ts.size() == 0
            ts.getEnd() == 100
            ts.getStart() == 1
        }

        where:
        withData << [true, false]

    }

    def emtpyFunctionValueMap() {
        return new FunctionValueMap(0, 0, 0)
    }

    def functionValueMapWithAg() {
        def functionValueMap = new FunctionValueMap(1, 0, 0)
        functionValueMap.add(new Percentile("[0.2d]"), 1000)
        functionValueMap
    }

    def functionValueMapWithAn() {
        def functionValueMap = new FunctionValueMap(0, 1, 0)
        functionValueMap.add(new Trend(), true, "")
        functionValueMap
    }

    def functionValueMapWithAn2() {
        def functionValueMap = new FunctionValueMap(0, 1, 0)
        functionValueMap.add(new Frequency(["20, 20"]), true, "identifier")
        functionValueMap
    }


    def functionValueMapWithTr() {
        def functionValueMap = new FunctionValueMap(0, 0, 1)
        functionValueMap.add(new Derivative())
        functionValueMap
    }

    def functionValueMapWithTr2() {
        def functionValueMap = new FunctionValueMap(0, 0, 1)
        functionValueMap.add(new Top(["2"]))
        functionValueMap
    }


    List<SolrDocument> fillDocs() {
        def result = new ArrayList<SolrDocument>()

        TimeSeriesConverter<MetricTimeSeries> converter = new MetricTimeSeriesConverter();

        10.times {
            MetricTimeSeries ts = new MetricTimeSeries.Builder("groovy","metric")
                    .attribute("host", "laptop")
                    .attribute("someInt", 1i + it)
                    .attribute("someFloat", 1.1f + it)
                    .attribute("someDouble", [2.0d + it])
                    .attribute("_version_", "ignored")
                    .points(times(it + 1), values(it + 1))
                    .build()
            def doc = converter.to(ts)
            result.add(asSolrDoc(doc))
        }

        result
    }

    def SolrDocument asSolrDoc(BinaryTimeSeries binaryStorageDocument) {
        def doc = new SolrDocument()
        doc.addField("host", binaryStorageDocument.get("host"))
        doc.addField("data", ByteBuffer.wrap(binaryStorageDocument.getPoints()))
        doc.addField("name", binaryStorageDocument.getName())
        doc.addField("type", binaryStorageDocument.getType())
        doc.addField("start", binaryStorageDocument.getStart())
        doc.addField("end", binaryStorageDocument.getEnd())
        doc.addField("someInt", binaryStorageDocument.get("someInt"))
        doc.addField("someFloat", binaryStorageDocument.get("someFloat"))
        doc.addField("someDouble", binaryStorageDocument.get("someDouble"))
        doc.addField("_version_", binaryStorageDocument.get("_version_"))
        doc.addField("userByteBuffer", ByteBuffer.wrap("some_user_bytes".bytes))


        doc
    }

    def LongList times(int i) {
        def times = new LongList()
        100.times {
            times.add(it * 15 + i as long)
        }
        times
    }

    def DoubleList values(int i) {
        def values = new DoubleList()

        100.times {
            values.add(it * 100 * i as double)
        }
        values
    }
}
