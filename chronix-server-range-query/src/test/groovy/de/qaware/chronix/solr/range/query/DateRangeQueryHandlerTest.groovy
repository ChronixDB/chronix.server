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
package de.qaware.chronix.solr.range.query

import de.qaware.chronix.test.extensions.ReflectionHelper
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.core.PluginInfo
import org.apache.solr.handler.component.SearchHandler
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import spock.lang.Specification

/**
 * Unit test for the date range query handler
 * @author f.lautenschlager
 */
class DateRangeQueryHandlerTest extends Specification {

    def "test get the description"() {
        given:
        def dateRangeQueryHandler = new DateRangeQueryHandler()

        when:
        def description = dateRangeQueryHandler.description

        then:
        description == "Chronix range query handler. Delegates to the default search handler"
    }


    def "test inform cores"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def dateRangeQueryHandler = new DateRangeQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", dateRangeQueryHandler)

        when:
        dateRangeQueryHandler.inform(null)

        then:
        1 * defaultHandler.inform(null)
    }

    def "test init"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def dateRangeQueryHandler = new DateRangeQueryHandler()

        def info = Mock(PluginInfo)

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", dateRangeQueryHandler)

        when:
        dateRangeQueryHandler.init(info)

        then:
        1 * defaultHandler.init(info)
    }

    def "test handle request body"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def dateRangeQueryHandler = new DateRangeQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", dateRangeQueryHandler)

        def request = Mock(SolrQueryRequest)
        def response = Mock(SolrQueryResponse)

        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
        modifiableSolrParams.add("q", "host:laptop AND start:NOW")
        request.getParams() >> modifiableSolrParams

        when:
        dateRangeQueryHandler.handleRequestBody(request, response)

        then:
        1 * defaultHandler.handleRequestBody(request, response)
    }

    def "test exception cases"() {
        given:
        def defaultHandler = Mock(SearchHandler)
        def dateRangeQueryHandler = new DateRangeQueryHandler()

        ReflectionHelper.setValueToFieldOfObject(defaultHandler, "searchHandler", dateRangeQueryHandler)
        def response = Mock(SolrQueryResponse)

        when:
        dateRangeQueryHandler.handleRequestBody(request, response)

        then:
        thrown Exception
        0 * dateRangeQueryHandler.handleRequestBody(request, response)

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
