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
package de.qaware.chronix.solr.query.analysis.collectors

import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.dts.MetricDataPoint
import de.qaware.chronix.solr.query.analysis.JoinFunctionEvaluator
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.lucene.document.*
import org.apache.lucene.util.BytesRef
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
        Map<String, List<Document>> collectedDocs = new HashMap<>();
        def document = new Document()
        document.add(new StringField("host", "laptop", Field.Store.NO))
        document.add(new StringField("metric", "groovy", Field.Store.NO))
        def fq = ["join=metric,host"] as String[]
        def joinFunction = JoinFunctionEvaluator.joinFunction(fq)

        when:
        2.times { AnalysisDocumentBuilder.collect(collectedDocs, document, joinFunction) }

        then:
        collectedDocs.size() == 1
        collectedDocs.get("groovy-laptop").size() == 2
    }

    def "test high level analysis"() {
        given:
        def collectedDocs = new HashMap<String, List<Document>>();

        def docs = fillDocs()
        collectedDocs.put("groovy-laptop", docs);
        def collectedDoc = collectedDocs.entrySet().iterator().next()
        def analysis = AnalysisQueryEvaluator.buildAnalysis(["analysis=frequency:10:10"] as String[])

        when:
        def document = AnalysisDocumentBuilder.analyze(analysis, 0l, Long.MAX_VALUE, collectedDoc);

        then:
        document == null

    }

    def "test aggregate"() {
        given:
        def collectedDocs = new HashMap<String, List<Document>>();

        def docs = fillDocs()
        collectedDocs.put("groovy-laptop", docs);
        def collectedDoc = collectedDocs.entrySet().iterator().next()
        def aggregation = AnalysisQueryEvaluator.buildAnalysis(["ag=max"] as String[])

        when:

        def document = AnalysisDocumentBuilder.analyze(aggregation, 0l, Long.MAX_VALUE, collectedDoc);

        then:
        document.getFieldValue("host") as String == "laptop"
        document.getFieldValue("metric") as String == "groovy"
        document.getFieldValue("start") as long == 0
        document.getFieldValue("end") as long == 891
        document.getFieldValue("value") as double == 89100.0d
        document.getFieldValue("analysis") as String == "MAX"
        document.getFieldValue("analysisParam") as String == ""
        document.getFieldValue("joinKey") as String == "groovy-laptop"

        document.getFieldValue("someInt") as int == 1i
        document.getFieldValue("someFloat") as float == 1.1f
        document.getFieldValue("someDouble") as double == 2.0d

    }

    ArrayList<Document> fillDocs() {
        def result = new ArrayList<Document>();

        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();

        10.times {
            MetricTimeSeries ts = new MetricTimeSeries.Builder("groovy")
                    .attribute("host", "laptop")
                    .data(createPoints(it))
                    .build();
            def doc = converter.to(ts)
            result.add(asLuceneDoc(doc))
        }

        result
    }

    def Document asLuceneDoc(BinaryStorageDocument binaryStorageDocument) {
        def doc = new Document()
        doc.add(new StringField("host", binaryStorageDocument.get("host").toString(), Field.Store.NO))
        doc.add(new BinaryDocValuesField("data", new BytesRef(binaryStorageDocument.getData())))
        doc.add(new StringField("metric", binaryStorageDocument.get("metric").toString(), Field.Store.NO))
        doc.add(new LongField("start", binaryStorageDocument.getStart(), Field.Store.NO))
        doc.add(new LongField("end", binaryStorageDocument.getEnd(), Field.Store.NO))
        doc.add(new IntField("someInt", 1i, Field.Store.NO))
        doc.add(new FloatField("someFloat", 1.1f, Field.Store.NO))
        doc.add(new DoubleField("someDouble", 2.0d, Field.Store.NO))

        doc
    }

    def createPoints(int i) {
        def points = new ArrayList<MetricDataPoint>()
        100.times {
            points.add(new MetricDataPoint(it * i, it * 100 * i))
        }
        points
    }
}
