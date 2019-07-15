/*
 * Copyright (C) 2018 QAware GmbH
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

import de.qaware.chronix.converter.common.Compression
import de.qaware.chronix.converter.serializer.protobuf.ProtoBufMetricTimeSeriesSerializer
import de.qaware.chronix.cql.CQLCFResult
import de.qaware.chronix.cql.ChronixFunctions
import de.qaware.chronix.server.functions.ChronixTransformation
import de.qaware.chronix.server.types.ChronixType
import de.qaware.chronix.solr.query.ChronixQueryParams
import de.qaware.chronix.solr.query.analysis.providers.SolrDocListProvider
import de.qaware.chronix.solr.type.metric.MetricType
import de.qaware.chronix.solr.type.metric.functions.aggregations.Max
import de.qaware.chronix.solr.type.metric.functions.aggregations.Min
import de.qaware.chronix.solr.type.metric.functions.analyses.Trend
import de.qaware.chronix.solr.type.metric.functions.transformation.Add
import de.qaware.chronix.timeseries.MetricTimeSeries
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.core.PluginInfo
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.schema.IndexSchema
import org.apache.solr.schema.SchemaField
import org.apache.solr.search.DocSlice
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.time.Instant

/**
 * Unit test for the analysis handler.
 * @author f.lautenschlager
 */
class AnalysisHandlerTest extends Specification {

    @Unroll
    def "test handle function request for #params"() {
        given:
        def request = Mock(SolrQueryRequest)
        def indexSchema = Mock(IndexSchema)

        indexSchema.getFields() >> new HashMap<String, SchemaField>()
        request.getSchema() >> indexSchema
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
                           .add(ChronixQueryParams.CHRONIX_FUNCTION, "metric{max}").add(ChronixQueryParams.QUERY_START_LONG, "0")
                           .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE))
                           .add("rows", "0"),
                   /*new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fl", "myfield,start,end,data,metric")
                           .add(ChronixQueryParams.CHRONIX_FUNCTION, "metric{fastdtw:(metric:* AND start:NOW),10,0.5}").add(ChronixQueryParams.QUERY_START_LONG, "0")
                           .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE)),*/
                   new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                           .add(ChronixQueryParams.CHRONIX_FUNCTION, "metric{trend}").add(ChronixQueryParams.QUERY_START_LONG, "0")
                           .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE))
                           .add("rows", "0"),
        ]

    }

    def "test get fields"() {
        given:
        def docListMock = Stub(DocListProvider)
        def analysisHandler = new AnalysisHandler(docListMock)

        when:
        def fields = analysisHandler.getFields(concatedFields, new HashMap<String, SchemaField>())

        then:
        fields == result

        where:
        concatedFields << [null, "myField,start,end,data,metric"]
        result << [new HashSet<>(), ["myField", "start", "end", "data", "metric"] as Set<String>]
    }

    @Shared
    def functions = new ChronixFunctions()

    @Unroll
    def "test single time series for #queryFunction"() {
        given:
        def docListMock = Stub(DocListProvider)
        def analysisHandler = new AnalysisHandler(docListMock)
        def start = Instant.now()
        HashMap<ChronixType, Map<String, List<SolrDocument>>> timeSeriesRecords = new HashMap<>()

        Map<String, List<SolrDocument>> metricTimeSeriesRecords = new HashMap<>()
        metricTimeSeriesRecords.put("something", solrDocument(start))
        timeSeriesRecords.put(new MetricType(), metricTimeSeriesRecords)

        def request = Mock(SolrQueryRequest)
        request.params >> new ModifiableSolrParams().add("q", "host:laptop AND start:NOW")
                .add(ChronixQueryParams.QUERY_START_LONG, "0")
                .add(ChronixQueryParams.QUERY_END_LONG, String.valueOf(Long.MAX_VALUE))

        when:
        //execute function
        function()

        def typeFunctions = new CQLCFResult()
        typeFunctions.addChronixFunctionsForType(new MetricType(), functions)
        def result = analysisHandler.analyze(request, typeFunctions, timeSeriesRecords)

        then:
        result.size() == 1
        result.get(0).get(resultKey) == expectedResult

        functions.clear()

        where:
        queryFunction << ["min",
                          "max",
                          "trend",
                          "add:5"]

        function << [{ -> functions.addAggregation(new Min()) },
                     { -> functions.addAggregation(new Max()) },
                     { -> functions.addAnalysis(new Trend()) },
                     { ->
                         ChronixTransformation<MetricTimeSeries> add = new Add()
                         add.setArguments(["5"] as String[])
                         functions.addTransformation(add)
                     }]

        resultKey << ["0_function_min",
                      "0_function_max",
                      "0_function_trend",
                      "0_function_add"]

        expectedResult << [4711, 4713, true, ["value=5.0"]]
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

    List<SolrDocument> solrDocument(Instant start) {
        def result = new ArrayList<SolrDocument>()
        def ts = new MetricTimeSeries.Builder("test", "metric")
                .point(start.toEpochMilli(), 4711)
                .point(start.plusSeconds(1).toEpochMilli(), 4712)
                .point(start.plusSeconds(2).toEpochMilli(), 4713)
                .build()

        SolrDocument doc = new SolrDocument()
        doc.put("start", start.toEpochMilli())
        doc.put("end", start.plusSeconds(10).toEpochMilli())
        doc.put("name", "test")
        doc.put("type", "metric")

        def data = ProtoBufMetricTimeSeriesSerializer.to(ts.points().iterator())
        def compressed = Compression.compress(data)
        doc.put("data", ByteBuffer.wrap(compressed))

        result.add(doc)
        return result
    }
}
