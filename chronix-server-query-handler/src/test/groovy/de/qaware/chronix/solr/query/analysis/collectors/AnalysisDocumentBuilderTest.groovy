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
package de.qaware.chronix.solr.query.analysis.collectors

import de.qaware.chronix.converter.BinaryTimeSeries
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.converter.TimeSeriesConverter
import de.qaware.chronix.solr.query.analysis.JoinFunctionEvaluator
import de.qaware.chronix.timeseries.MetricTimeSeries
import de.qaware.chronix.timeseries.dt.DoubleList
import de.qaware.chronix.timeseries.dt.LongList
import org.apache.lucene.document.Document
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import spock.lang.Specification

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


    def "test aggregate"() {
        given:
        def docs = fillDocs()
        def aggregation = AnalysisQueryEvaluator.buildAnalysis(["ag=max"] as String[])

        when:
        def document = AnalysisDocumentBuilder.aggregate(aggregation, 0l, Long.MAX_VALUE, "groovy-laptop", docs);

        then:
        document.getFieldValue("host") as String == "laptop"
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 1495
        document.getFieldValue("value") as double == 99000.0d
        document.getFieldValue("analysis") as String == "MAX"
        document.getFieldValue("analysisParam") as Object[] == new Object[0]
        document.getFieldValue("joinKey") as String == "groovy-laptop"
        document.getFieldValue("data") == null

        document.getFieldValue("someInt") as int == 1i
        document.getFieldValue("someFloat") as float == 1.1f
        document.getFieldValue("someDouble") as double == 2.0d

    }

    def "test analyze"() {
        given:
        def docs = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=trend"] as String[])

        when:
        def document = AnalysisDocumentBuilder.analyze(analysis, 0l, Long.MAX_VALUE, "groovy-laptop", docs);

        then:
        document.getFieldValue("host") as String == "laptop"
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 1495
        document.getFieldValue("analysis") as String == "TREND"
        document.getFieldValue("analysisParam") as Object[] == new Object[0]
        document.getFieldValue("joinKey") as String == "groovy-laptop"
        document.getFieldValue("data") != null

        document.getFieldValue("someInt") as int == 1i
        document.getFieldValue("someFloat") as float == 1.1f
        document.getFieldValue("someDouble") as double == 2.0d
    }

    def "test analyze with subquery"() {
        given:
        def docs = fillDocs()
        def docs2 = fillDocs()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=fastdtw:(metric:*),10,0.5"] as String[])

        when:
        def document = AnalysisDocumentBuilder.analyze(analysis, 0l, Long.MAX_VALUE, "groovy-laptop", docs, docs2);

        then:
        document.getFieldValue("host") as String == "laptop"
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 1
        document.getFieldValue("end") as long == 1495
        document.getFieldValue("analysis") as String == "FASTDTW"
        (document.getFieldValue("analysisParam") as Object[]).length == 3
        document.getFieldValue("joinKey") as String == "groovy-laptop"
        document.getFieldValue("data") != null

        document.getFieldValue("someInt") as int == 1i
        document.getFieldValue("someFloat") as float == 1.1f
        document.getFieldValue("someDouble") as double == 2.0d
    }

    List<Document> fillDocs() {
        def result = new ArrayList<SolrDocument>();

        TimeSeriesConverter<MetricTimeSeries> converter = new KassiopeiaSimpleConverter();

        10.times {
            MetricTimeSeries ts = new MetricTimeSeries.Builder("groovy")
                    .attribute("host", "laptop")
                    .data(times(it + 1), values(it + 1))
                    .build();
            def doc = converter.to(ts)
            result.add(asSolrDoc(doc))
        }

        result
    }

    def SolrDocument asSolrDoc(BinaryTimeSeries binaryStorageDocument) {
        def doc = new SolrDocument()
        doc.addField("host", binaryStorageDocument.get("host"))
        doc.addField("data", binaryStorageDocument.getPoints())
        doc.addField("metric", binaryStorageDocument.get("metric"))
        doc.addField("start", binaryStorageDocument.getStart())
        doc.addField("end", binaryStorageDocument.getEnd())
        doc.addField("someInt", 1i)
        doc.addField("someFloat", 1.1f)
        doc.addField("someDouble", 2.0d)

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
