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
package de.qaware.chronix.solr.query

import de.qaware.chronix.solr.query.analysis.AnalysisHandler
import de.qaware.chronix.solr.test.extensions.ReflectionHelper
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.common.util.NamedList
import org.apache.solr.core.PluginInfo
import org.apache.solr.handler.component.SearchHandler
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.schema.IndexSchema
import org.apache.solr.schema.SchemaField
import org.apache.solr.schema.TextField
import spock.lang.Specification

/**
 * Unit test for the date range query handler
 * @author f.lautenschlager
 */
class ChronixQueryHandlerTest extends Specification {

    def "test get the description"() {
        given:
        def dateRangeQueryHandler = new ChronixQueryHandler()

        when:
        def description = dateRangeQueryHandler.description

        then:
        description == "Chronix range query handler. Delegates to the default search handler"
    }


    def "test inform cores"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def aggregationHandler = Mock(AnalysisHandler.class)

        def chronixQueryHandler = new ChronixQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", chronixQueryHandler)
        ReflectionHelper.setValueToFieldOfObject(aggregationHandler, "analysisHandler", chronixQueryHandler)

        when:
        chronixQueryHandler.inform(null)

        then:
        1 * defaultHandler.inform(null)
        1 * aggregationHandler.inform(null)
    }

    def "test init"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def aggregationHandler = Mock(AnalysisHandler.class)

        def chronixQueryHandler = new ChronixQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", chronixQueryHandler)
        ReflectionHelper.setValueToFieldOfObject(aggregationHandler, "analysisHandler", chronixQueryHandler)

        def info = Mock(PluginInfo)

        when:
        chronixQueryHandler.init(info)

        then:
        1 * defaultHandler.init(info)
        1 * aggregationHandler.init(info)
    }

    def "test handle default request"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def analysisHandler = Mock(AnalysisHandler.class)

        def chronixQueryHandler = new ChronixQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", chronixQueryHandler)
        ReflectionHelper.setValueToFieldOfObject(analysisHandler, "analysisHandler", chronixQueryHandler)


        def request = Mock(SolrQueryRequest)
        def response = Mock(SolrQueryResponse)
        def responseHeader = new NamedList<Object>()
        def indexSchema = Mock(IndexSchema)

        indexSchema.getFields() >> ["data": new SchemaField("data", new TextField()), "metric": new SchemaField("metric", new TextField())]
        request.getSchema() >> indexSchema
        request.getParams() >> modifiableSolrParams
        response.getResponseHeader() >> responseHeader

        when:
        chronixQueryHandler.handleRequestBody(request, response)

        then:
        defaultHandlerCount * defaultHandler.handleRequestBody(request, response)
        analysisHandlerCount * analysisHandler.handleRequestBody(request, response)

        where:
        modifiableSolrParams << [new ModifiableSolrParams().add("q", "host:laptop AND start:NOW"),
                                 new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fl", null),
                                 new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fl", ""),
                                 new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fq", null),
                                 new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fq", ""),
                                 new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fq", "join=host,metric")]

        defaultHandlerCount << [1,1,1,1,1,0]
        analysisHandlerCount << [0,0,0,0,0,1]
    }

    def "test handle aggregation request"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def aggregationHandler = Mock(AnalysisHandler.class)

        def chronixQueryHandler = new ChronixQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", chronixQueryHandler)
        ReflectionHelper.setValueToFieldOfObject(aggregationHandler, "analysisHandler", chronixQueryHandler)

        def request = Mock(SolrQueryRequest)
        def response = Mock(SolrQueryResponse)
        def responseHeader = new NamedList<Object>()
        def indexSchema = Mock(IndexSchema)

        indexSchema.getFields() >> ["data": new SchemaField("data", new TextField()), "metric": new SchemaField("metric", new TextField())]
        request.getSchema() >> indexSchema

        request.getParams() >> modifiableSolrParams
        response.getResponseHeader() >> responseHeader

        when:
        chronixQueryHandler.handleRequestBody(request, response)

        then:
        0 * defaultHandler.handleRequestBody(request, response)
        1 * aggregationHandler.handleRequestBody(request, response)
        responseHeader.size() == 2
        def queryStart = responseHeader.get(ChronixQueryParams.QUERY_START_LONG) as long
        queryStart > 0
        def queryEnd = responseHeader.get(ChronixQueryParams.QUERY_END_LONG) as long
        queryEnd > 0

        where:
        modifiableSolrParams << [new ModifiableSolrParams().add("q", "host:laptop AND start:NOW").add("fq", "join=host,metric", "function=max")]

    }

    def "test exception cases"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def aggregationHandler = Mock(AnalysisHandler.class)


        def chronixQueryHandler = new ChronixQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", chronixQueryHandler)
        ReflectionHelper.setValueToFieldOfObject(aggregationHandler, "analysisHandler", chronixQueryHandler)

        def response = Mock(SolrQueryResponse)

        when:
        chronixQueryHandler.handleRequestBody(request, response)

        then:
        thrown Exception
        0 * aggregationHandler.handleRequestBody(request, response)
        0 * defaultHandler.handleRequestBody(request, response)

        where:
        request << [null, Mock(SolrQueryRequest), "invalid request"()]
    }

    def "invalid request"() {
        def request = Mock(SolrQueryRequest)

        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
        request.getParams() >> modifiableSolrParams

        return request
    }


}
