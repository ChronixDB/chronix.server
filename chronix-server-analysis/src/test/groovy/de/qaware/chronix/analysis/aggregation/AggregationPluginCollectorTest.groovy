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
package de.qaware.chronix.analysis.aggregation

import de.qaware.chronix.converter.BinaryStorageDocument
import de.qaware.chronix.converter.KassiopeiaSimpleConverter
import de.qaware.chronix.dts.MetricDataPoint
import de.qaware.chronix.test.extensions.ReflectionHelper
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.lucene.document.*
import org.apache.lucene.index.LeafReader
import org.apache.lucene.index.LeafReaderContext
import org.apache.lucene.util.BytesRef
import org.apache.solr.common.util.NamedList
import org.apache.solr.handler.component.ResponseBuilder
import org.apache.solr.handler.component.SearchComponent
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import spock.lang.Specification

/**
 * Created by f.lautenschlager on 26.11.2015.
 */
class AggregationPluginCollectorTest extends Specification {

    def "test aggregation collection"() {
        given:
        def responseBuilder = Mock(ResponseBuilder)
        def aggregation = "max";
        def queryStart = 0
        def queryEnd = 0

        def context = new LeafReaderContext(Mock(LeafReader))
        def aggregationCollector = new AggregationCollector(responseBuilder, aggregation, queryStart, queryEnd)

        ReflectionHelper.setValueToFieldOfObject(context, "context", aggregationCollector)

        when:
        aggregationCollector.collect(0)

        then:
        def collectedDoc = aggregationCollector.collectedDocs.get(null)
        collectedDoc.size() == 1
    }

    def "test finish aggregation"() {
        given:
        def solrResponse = new SolrQueryResponse()
        def responseBuilder = new ResponseBuilder(Mock(SolrQueryRequest), solrResponse, new ArrayList<SearchComponent>())
        def metric = "\\Load\\avg";
        def aggregationCollector = new AggregationCollector(responseBuilder, aggregation, 0, 990)


        aggregationCollector.collectedDocs.put(metric, createSomeSolrDocuments(metric))

        when:
        aggregationCollector.finish()

        then:
        def result = (NamedList) responseBuilder.rsp.values.get("aggregation")
        def metricMap = (Map) result.get(metric)
        metricMap.get("type") == aggregation

        where:

        aggregation << ["min", "max", "avg", "p=0.5", "dev"]

    }

    def createSomeSolrDocuments(String metric) {
        def documents = new ArrayList<Document>()

        10.times {
            MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder(metric)
            100.times {
                timeSeries.point(new MetricDataPoint(it * 10, it * 0.6515 * 100))
            }
            KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter()
            BinaryStorageDocument doc = converter.to(timeSeries.build())

            Document solrDoc = new Document()
            solrDoc.add(new StringField("metric", metric, Field.Store.YES))
            solrDoc.add(new BinaryDocValuesField("data", new BytesRef(doc.data)))
            solrDoc.add(new LongField("start", doc.start, Field.Store.YES))
            solrDoc.add(new LongField("end", doc.end, Field.Store.YES))

            documents.add(solrDoc)
        }

        documents
    }
}
