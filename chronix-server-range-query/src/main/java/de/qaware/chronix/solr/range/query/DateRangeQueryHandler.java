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
package de.qaware.chronix.solr.range.query;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;

/**
 * The date range query handler to convert date expression and
 * delegate the query to the default search handler
 *
 * @author f.lautenschlager
 */
public class DateRangeQueryHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {

    private static final String QUERY_PARAMETER = "q";
    private static final String QUERY_START_LONG = "query_start_long";
    private static final String QUERY_END_LONG = "query_end_long";

    /**
     * The default solr search handler
     */
    private final SearchHandler searchHandler = new SearchHandler();

    private final String[] dateFields = new String[]{"start:", "end:"};
    private final DateQueryParser dateRangeParser = new DateQueryParser(dateFields);


    @Override
    public void init(PluginInfo info) {
        searchHandler.init(info);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(req.getParams());

        String originQuery = modifiableSolrParams.get(QUERY_PARAMETER);

        long[] startAndEnd = dateRangeParser.getNumericQueryTerms(originQuery);

        modifiableSolrParams.set(QUERY_START_LONG, String.valueOf(startAndEnd[0]));
        modifiableSolrParams.set(QUERY_END_LONG, String.valueOf(startAndEnd[1]));

        String query = dateRangeParser.replaceRangeQueryTerms(originQuery);

        modifiableSolrParams.set(QUERY_PARAMETER, query);
        //Set the updated query
        req.setParams(modifiableSolrParams);

        //let the default search handler do its work
        searchHandler.handleRequestBody(req, rsp);
    }


    @Override
    public String getDescription() {
        return "Chronix range query handler. Delegates to the default search handler";
    }

    @Override
    public void inform(SolrCore core) {
        searchHandler.inform(core);
    }
}
