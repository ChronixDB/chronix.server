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

import de.qaware.chronix.converter.serializer.ProtoBufKassiopeiaSimpleSerializer
import de.qaware.chronix.solr.query.ChronixQueryParams
import de.qaware.chronix.solr.query.analysis.functions.aggregations.Max
import de.qaware.chronix.solr.query.analysis.functions.analyses.FastDtw
import de.qaware.chronix.solr.query.analysis.providers.SolrDocListProvider
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.core.PluginInfo
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.search.DocSlice
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.Instant
import java.util.function.Function

/**
 * Unit test for the analysis handler.
 * @author f.lautenschlager
 */
class AnalysisHandlerTest extends Specification {

    def "test handle aggregation request"() {
        given:
        def request = Mock(SolrQueryRequest)

        request.getParams() >> params

        def docListMock = Stub(DocListProvider)
        docListMock.doSimpleQuery(_, _, _, _) >> { new DocSlice(0i, 0, [] as int[], [] as float[], 0, 0) }

        def analysisHandler = new AnalysisHandler(docListMock)

        when:
        analysisHandler.handleRequestBody(request, Mock(SolrQueryResponse))

        then:
        noExceptionThrown()

        where:
        params << [new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                           .add("rows", "0"),
                   new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                           .add("fq", "ag=max").add(ChronixQueryParams.QUERY_START_LONG, "0")
                           .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE)),
                   new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fl", "myfield,start,end,data,metric")
                           .add("fq", "analysis=fastdtw:(metric:* AND start:NOW),10,0.5").add(ChronixQueryParams.QUERY_START_LONG, "0")
                           .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE)),
                   new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                           .add("fq", "analysis=trend").add(ChronixQueryParams.QUERY_START_LONG, "0")
                           .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE)),
        ]

    }

    def "test get fields"() {
        given:
        def docListMock = Stub(DocListProvider)
        def analysisHandler = new AnalysisHandler(docListMock)

        when:
        def fields = analysisHandler.getFields(concatedFields)

        then:
        fields == result

        where:
        concatedFields << [null, "myField,start,end,data,metric"]
        result << [null, ["myField", "start", "end", "data", "metric"] as Set<String>]
    }

    def "test analyze / aggregate single time series"() {
        given:
        def docListMock = Stub(DocListProvider)
        def analysisHandler = new AnalysisHandler(docListMock)
        def start = Instant.now()
        Map<String, List<SolrDocument>> timeSeriesRecords = new HashMap<>()
        timeSeriesRecords.put("something", solrDocument(start))

        def request = Mock(SolrQueryRequest)
        request.params >> new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                .add("fq", "function=max").add(ChronixQueryParams.QUERY_START_LONG, "0")
                .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE))
        def analyses = new QueryFunctions<>()
        analyses.addAggregation(new Max())
        Function<SolrDocument, String> key = JoinFunctionEvaluator.joinFunction(null);


        when:
        def result = analysisHandler.analyze(request, analyses, key, timeSeriesRecords)

        then:
        result.size() == 1
        result.get(0).get("0_function_max") == 4713
    }

    //TODO: Fix test.
    def "test analyze multiple time series"() {
        given:
        def docListMock = Stub(DocListProvider)
        def analysisHandler = new AnalysisHandler(docListMock)
        def start = Instant.now();

        Map<String, List<SolrDocument>> timeSeriesRecords = new HashMap<>()
        timeSeriesRecords.put("something", solrDocument(start))

        Map<String, List<SolrDocument>> timeSeriesRecordsFromSubQuery = new HashMap<>()
        timeSeriesRecordsFromSubQuery.put("something", solrDocument(start))
        timeSeriesRecordsFromSubQuery.put("something-other", solrDocument(start))

        def request = Mock(SolrQueryRequest)
        request.params >> new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                .add("fq", "ag=max").add(ChronixQueryParams.QUERY_START_LONG, "0")
                .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE))
        def analyses = new QueryFunctions<>()
        analyses.addAnalysis(new FastDtw("ignored", 1, 0.8))
        Function<SolrDocument, String> key = JoinFunctionEvaluator.joinFunction(null);

        when:
        analysisHandler.metaClass.collectDocuments = { -> return timeSeriesRecordsFromSubQuery }
        def result = analysisHandler.analyze(request, analyses, key, timeSeriesRecords)

        then:
        result.size() == 0
        /*
        result.get(0).get("function_value") == 0.0
        result.get(0).get("metric") == "test"
        result.get(0).get("join_key") == "something-other"
        */
    }

    List<SolrDocument> solrDocument(Instant start) {
        def result = new ArrayList<SolrDocument>()
        def ts = new MetricTimeSeries.Builder("test")
                .point(start.toEpochMilli(), 4711)
                .point(start.plusSeconds(1).toEpochMilli(), 4712)
                .point(start.plusSeconds(2).toEpochMilli(), 4713)
                .build()
        SolrDocument doc = new SolrDocument()
        doc.put("start", start.toEpochMilli())
        doc.put("end", start.plusSeconds(10).toEpochMilli())
        doc.put("metric", "test")
        def data = ProtoBufKassiopeiaSimpleSerializer.to(ts.points().iterator())
        doc.put("data", ByteBuffer.wrap(data))

        result.add(doc)
        return result
    }

    def "test get description"() {
        given:
        def analysisHandler = new AnalysisHandler(new SolrDocListProvider())

        when:
        def description = analysisHandler.getDescription()

        then:
        description == "Chronix Aggregation Request Handler"
    }

    def "test init and inform"() {
        given:
        def pluginInfo = Mock(PluginInfo.class)
        def analysisHandler = new AnalysisHandler(new SolrDocListProvider())

        when:
        analysisHandler.init(pluginInfo)
        analysisHandler.inform(null)

        then:
        thrown NullPointerException
    }
}
