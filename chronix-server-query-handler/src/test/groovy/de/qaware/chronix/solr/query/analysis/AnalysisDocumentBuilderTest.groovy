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

import de.qaware.chronix.converter.BinaryTimeSeries
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.converter.TimeSeriesConverter
import de.qaware.chronix.timeseries.MetricTimeSeries
import de.qaware.chronix.timeseries.dt.DoubleList
import de.qaware.chronix.timeseries.dt.LongList
import org.apache.lucene.document.Document
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * @author f.lautenschlager
 */
class AnalysisDocumentBuilderTest extends Specification {

    def "test private constructor"() {
        when:
        AnalysisDocumentBuilder.newInstance()
        then:
        noExceptionThrown()
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
        def joinFunction = JoinFunctionEvaluator.joinFunction(fq)

        when:
        def collectedDocs = AnalysisDocumentBuilder.collect(docs, joinFunction)

        then:
        collectedDocs.size() == 1
        collectedDocs.get("groovy-laptop").size() == 2
    }

    def "test high level analysis returns null"() {
        given:
        def docs = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=frequency:0,999999"] as String[])

        when:
        def ts = AnalysisDocumentBuilder.collectDocumentToTimeSeries(0l, Long.MAX_VALUE, docs);
        def document = AnalysisDocumentBuilder.analyze(analysis, "groovy-laptop", ts);

        then:
        document == null
    }

    def "test aggregate"() {
        given:
        def docs = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["ag=max"] as String[])

        when:
        def ts = AnalysisDocumentBuilder.collectDocumentToTimeSeries(0l, Long.MAX_VALUE, docs);
        def document = AnalysisDocumentBuilder.analyze(analysis, "groovy-laptop", ts);

        then:
        document.getFieldValue("host") as Set == ["laptop"] as Set<String>
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 1495
        document.getFieldValue("function") as String == "MAX"
        document.getFieldValue("function_value") as double == 99000.0d
        document.getFieldValue("function_arguments") as Object[] == new Object[0]
        document.getFieldValue("join_key") as String == "groovy-laptop"
        document.getFieldValue("data") == null
        document.getFieldValue("_version_") == null
        (document.getFieldValue("userByteBuffer") as Set).size() == 10

        document.getFieldValue("someInt") as Set == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as Set<Integer>
        document.getFieldValue("someFloat") as Set == [9.100000023841858, 5.100000023841858, 8.100000023841858, 3.100000023841858, 4.100000023841858, 10.100000023841858, 1.100000023841858, 7.100000023841858, 2.100000023841858, 6.100000023841858] as Set<Float>
        document.getFieldValue("someDouble") as Set == [2.0, 4.0, 8.0, 9.0, 5.0, 10.0, 11.0, 3.0, 6.0, 7.0] as Set<Double>
    }

    def "test analyze"() {
        given:
        def docs = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=trend"] as String[])

        when:
        def ts = AnalysisDocumentBuilder.collectDocumentToTimeSeries(0l, Long.MAX_VALUE, docs);
        def document = AnalysisDocumentBuilder.analyze(analysis, "groovy-laptop", ts);

        then:
        document.getFieldValue("host") as Set == ["laptop"] as Set<String>
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 1495
        document.getFieldValue("function") as String == "TREND"
        document.getFieldValue("function_value") as double == 1
        document.getFieldValue("function_arguments") as Object[] == new Object[0]
        document.getFieldValue("join_key") as String == "groovy-laptop"
        document.getFieldValue("data") != null
        document.getFieldValue("_version_") == null
        (document.getFieldValue("userByteBuffer") as Set).size() == 10

        document.getFieldValue("someInt") as Set == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as Set<Integer>
        document.getFieldValue("someFloat") as Set == [9.100000023841858, 5.100000023841858, 8.100000023841858, 3.100000023841858, 4.100000023841858, 10.100000023841858, 1.100000023841858, 7.100000023841858, 2.100000023841858, 6.100000023841858] as Set<Float>
        document.getFieldValue("someDouble") as Set == [2.0, 4.0, 8.0, 9.0, 5.0, 10.0, 11.0, 3.0, 6.0, 7.0] as Set<Double>
    }

    def "test analyze with subquery"() {
        given:
        def docs = fillDocs()
        def docs2 = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=fastdtw:(metric:*),10,0.5"] as String[])

        when:
        def ts = AnalysisDocumentBuilder.collectDocumentToTimeSeries(0l, Long.MAX_VALUE, docs);
        def ts2 = AnalysisDocumentBuilder.collectDocumentToTimeSeries(0l, Long.MAX_VALUE, docs2);
        def document = AnalysisDocumentBuilder.analyze(analysis, "groovy-laptop", ts, ts2);

        then:
        document.getFieldValue("host") as Set<String> == ["laptop"] as Set<String>
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 1495
        document.getFieldValue("function") as String == "FASTDTW"
        (document.getFieldValue("function_arguments") as Object[]).length == 3
        document.getFieldValue("function_value") as double == 0
        document.getFieldValue("join_key") as String == "groovy-laptop"
        document.getFieldValue("data") != null
        document.getFieldValue("_version_") == null
        (document.getFieldValue("userByteBuffer") as Set).size() == 10

        document.getFieldValue("someInt") as Set == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as Set<Integer>
        document.getFieldValue("someFloat") as Set == [9.100000023841858, 5.100000023841858, 8.100000023841858, 3.100000023841858, 4.100000023841858, 10.100000023841858, 1.100000023841858, 7.100000023841858, 2.100000023841858, 6.100000023841858] as Set<Float>
        document.getFieldValue("someDouble") as Set == [2.0, 4.0, 8.0, 9.0, 5.0, 10.0, 11.0, 3.0, 6.0, 7.0] as Set<Double>
    }

    List<Document> fillDocs() {
        def result = new ArrayList<SolrDocument>();

        TimeSeriesConverter<MetricTimeSeries> converter = new KassiopeiaSimpleConverter();

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
