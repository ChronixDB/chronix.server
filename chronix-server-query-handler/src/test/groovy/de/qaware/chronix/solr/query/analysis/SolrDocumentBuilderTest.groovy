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
package de.qaware.chronix.solr.query.analysis

import de.qaware.chronix.Schema
import de.qaware.chronix.converter.BinaryTimeSeries
import de.qaware.chronix.converter.MetricTimeSeriesConverter
import de.qaware.chronix.converter.TimeSeriesConverter
import de.qaware.chronix.converter.common.DoubleList
import de.qaware.chronix.converter.common.LongList
import de.qaware.chronix.solr.query.ChronixQueryParams
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap
import de.qaware.chronix.solr.query.analysis.functions.aggregations.Percentile
import de.qaware.chronix.solr.query.analysis.functions.analyses.Frequency
import de.qaware.chronix.solr.query.analysis.functions.analyses.Trend
import de.qaware.chronix.solr.query.analysis.functions.transformation.Derivative
import de.qaware.chronix.solr.query.analysis.functions.transformation.Top
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
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
    def "test build document with data as json: #dataAsJson and data should returned #dataShouldReturned"() {
        given:
        def timeSeries = new MetricTimeSeries.Builder("test-build-document")
        10.times {
            timeSeries.point(it * 100, it + 100)
        }
        timeSeries.attribute("test", "groovy")
        timeSeries.attribute("list", [1, 2, 3, 4] as List<Integer>)


        when:
        def document = SolrDocumentBuilder.buildDocument(timeSeries.build(), null, "test", dataShouldReturned, dataAsJson)

        then:
        document != null
        document.containsKey(Schema.DATA) == containsDataAsBin
        document.containsKey(ChronixQueryParams.DATA_AS_JSON) == containsDataAsJson


        where:
        dataAsJson << [false, true, false, false, true]
        dataShouldReturned << [false, false, true, true, true]
        containsDataAsBin << [false, false, true, true, false]
        containsDataAsJson << [false, false, false, false, true]
    }

    @Unroll
    def "test build document with analyses #functionValues"() {
        given:
        def timeSeries = new MetricTimeSeries.Builder("test-build-document")
        10.times {
            timeSeries.point(it * 100, it + 100)
        }
        timeSeries.attribute("test", "groovy")
        timeSeries.attribute("list", [1, 2, 3, 4] as List<Integer>)


        when:
        def document = SolrDocumentBuilder.buildDocument(timeSeries.build(), functionValues, "test", true, false)

        then:
        document != null
        document.get(ChronixQueryParams.JOIN_KEY) == "test"
        document.containsKey(functionName)
        document.size() == nrOfAttributes

        where:
        functionValues << [functionValueMapWithAg(),
                           functionValueMapWithAn(),
                           functionValueMapWithAn2(),
                           functionValueMapWithTr(),
                           functionValueMapWithTr2()]

        functionName << ["0_function_p",
                         "0_function_trend",
                         "0_function_frequency_identifier",
                         "0_function_derivative",
                         "0_function_top"]

        nrOfAttributes << [9, 8, 9, 8, 8]
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
        functionValueMap.add(new Percentile(0.2d), 1000)
        functionValueMap
    }

    def functionValueMapWithAn() {
        def functionValueMap = new FunctionValueMap(0, 1, 0)
        functionValueMap.add(new Trend(), true, "")
        functionValueMap
    }

    def functionValueMapWithAn2() {
        def functionValueMap = new FunctionValueMap(0, 1, 0)
        functionValueMap.add(new Frequency(20, 20), true, "identifier")
        functionValueMap
    }


    def functionValueMapWithTr() {
        def functionValueMap = new FunctionValueMap(0, 0, 1)
        functionValueMap.add(new Derivative())
        functionValueMap
    }

    def functionValueMapWithTr2() {
        def functionValueMap = new FunctionValueMap(0, 0, 1)
        functionValueMap.add(new Top(2))
        functionValueMap
    }


    def "test collect"() {
        given:
        def docs = new SolrDocumentList()

        2.times {
            def document = new SolrDocument()
            document.addField("host", "laptop")
            document.addField("metric", "groovy")

            docs.add(document);
        }

        def fq = ["join=metric,host"] as String[]
        def joinFunction = new JoinFunction(fq)

        when:
        def collectedDocs = SolrDocumentBuilder.collect(docs, joinFunction)

        then:
        collectedDocs.size() == 1
        collectedDocs.get("groovy-laptop").size() == 2
    }

    List<SolrDocument> fillDocs() {
        def result = new ArrayList<SolrDocument>();

        TimeSeriesConverter<MetricTimeSeries> converter = new MetricTimeSeriesConverter();

        10.times {
            MetricTimeSeries ts = new MetricTimeSeries.Builder("groovy")
                    .attribute("host", "laptop")
                    .attribute("someInt", 1i + it)
                    .attribute("someFloat", 1.1f + it)
                    .attribute("someDouble", [2.0d + it])
                    .attribute("_version_", "ignored")
                    .points(times(it + 1), values(it + 1))
                    .build();
            def doc = converter.to(ts)
            result.add(asSolrDoc(doc))
        }

        result
    }

    def SolrDocument asSolrDoc(BinaryTimeSeries binaryStorageDocument) {
        def doc = new SolrDocument()
        doc.addField("host", binaryStorageDocument.get("host"))
        doc.addField("data", ByteBuffer.wrap(binaryStorageDocument.getPoints()))
        doc.addField("metric", binaryStorageDocument.get("metric"))
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
