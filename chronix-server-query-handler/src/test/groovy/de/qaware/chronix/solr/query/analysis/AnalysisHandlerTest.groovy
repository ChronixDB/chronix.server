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
package de.qaware.chronix.solr.query.analysis

import de.qaware.chronix.solr.query.ChronixQueryParams
import de.qaware.chronix.solr.query.analysis.providers.SolrDocListProvider
import org.apache.solr.common.params.ModifiableSolrParams
import org.apache.solr.core.PluginInfo
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.search.DocSlice
import spock.lang.Ignore
import spock.lang.Specification

/**
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
        ]

    }

    def "test get description"() {
        given:
        def analysisHandler = new AnalysisHandler(new SolrDocListProvider())

        when:
        def description = analysisHandler.getDescription()

        then:
        description == "Chronix Aggregation Request Handler"
    }

    @Ignore
    def "test init and inform"() {
        given:
        def pluginInfo = Mock(PluginInfo.class)
        def analysisHandler = new AnalysisHandler(new SolrDocListProvider())

        when:
        analysisHandler.init(pluginInfo)
        analysisHandler.inform(null)

        then:
        noExceptionThrown()
    }
}
