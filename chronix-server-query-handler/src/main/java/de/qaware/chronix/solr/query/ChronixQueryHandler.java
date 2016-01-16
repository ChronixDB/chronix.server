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
package de.qaware.chronix.solr.query;

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.common.MetricTSSchema;
import de.qaware.chronix.solr.query.analysis.AnalysisHandler;
import de.qaware.chronix.solr.query.analysis.providers.SolrDocListProvider;
import de.qaware.chronix.solr.query.date.DateQueryParser;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;

import java.util.HashSet;
import java.util.Set;

/**
 * The date range query handler to convert date expression and
 * delegate the query to the default search handler
 *
 * @author f.lautenschlager
 */
public class ChronixQueryHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {

    private static final Set<String> REQUIRED_FIELDS = new HashSet<>();
    /**
     * The default solr search handler
     */
    private final SearchHandler searchHandler = new SearchHandler();

    /**
     * The isAggregation handler
     */
    private final SearchHandler analysisHandler = new AnalysisHandler(new SolrDocListProvider());

    /**
     * The date range parser
     */
    private final DateQueryParser dateRangeParser = new DateQueryParser(new String[]{ChronixQueryParams.DATE_START_FIELD, ChronixQueryParams.DATE_END_FIELD});


    static {
        REQUIRED_FIELDS.add(Schema.DATA);
        REQUIRED_FIELDS.add(Schema.START);
        REQUIRED_FIELDS.add(Schema.END);
        REQUIRED_FIELDS.add(MetricTSSchema.METRIC);
    }

    @Override
    public void init(PluginInfo info) {
        searchHandler.init(info);
        analysisHandler.init(info);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(req.getParams());

        String originQuery = modifiableSolrParams.get(CommonParams.Q);

        long[] startAndEnd = dateRangeParser.getNumericQueryTerms(originQuery);
        long queryStart = or(startAndEnd[0], -1, 0);
        long queryEnd = or(startAndEnd[1], -1, Long.MAX_VALUE);

        modifiableSolrParams.set(ChronixQueryParams.QUERY_START_LONG, String.valueOf(queryStart));
        modifiableSolrParams.set(ChronixQueryParams.QUERY_END_LONG, String.valueOf(queryEnd));

        String query = dateRangeParser.replaceRangeQueryTerms(originQuery);

        modifiableSolrParams.set(CommonParams.Q, query);

        //Set the min required fields if the user define a sub set of fields
        modifiableSolrParams.set(CommonParams.FL, minRequiredFields(modifiableSolrParams.get(CommonParams.FL)));
        //Set the updated query
        req.setParams(modifiableSolrParams);

        //check the filter queries
        String[] filterQueries = modifiableSolrParams.getParams(CommonParams.FQ);

        //if we have an isAggregation
        if (contains(filterQueries, ChronixQueryParams.AGGREGATION_PARAM) || contains(filterQueries, ChronixQueryParams.ANALYSIS_PARAM)) {
            analysisHandler.handleRequestBody(req, rsp);

        } else {
            //let the default search handler do its work
            searchHandler.handleRequestBody(req, rsp);
        }

        //add the converted start and end to the response
        rsp.getResponseHeader().add(ChronixQueryParams.QUERY_START_LONG, queryStart);
        rsp.getResponseHeader().add(ChronixQueryParams.QUERY_END_LONG, queryEnd);
    }

    private long or(long value, long condition, long or) {
        if (value == condition) {
            return or;
        } else {
            return value;
        }
    }

    /**
     * Gets the requested fields.
     * Joins the REQUIRED_FIELDS and the user defined fields.
     * E.g.:
     * user requested fields: userField
     * => data,start,end,metric,userField
     *
     * @param fl - the solr fl param
     * @return the user defined fields and the required fields, or null if fl is null
     */
    private String minRequiredFields(String fl) {
        //As a result Solr will return everything
        if (fl == null) {
            return null;
        }
        return fl + ChronixQueryParams.JOIN_SEPARATOR + String.join(ChronixQueryParams.JOIN_SEPARATOR, REQUIRED_FIELDS);
    }

    /**
     * Checks if the given string array (filter queries) contains the given identifier.
     *
     * @param filterQueries - the filter queries
     * @param identifier    - an identifier
     * @return true if the filter queries contains the identifier, otherwise false.
     */
    private boolean contains(String[] filterQueries, String identifier) {
        if (filterQueries == null) {
            return false;
        }

        for (String filterQuery : filterQueries) {
            if (filterQuery.contains(identifier)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String getDescription() {
        return "Chronix range query handler. Delegates to the default search handler";
    }

    @Override
    public void inform(SolrCore core) {
        searchHandler.inform(core);
        analysisHandler.inform(core);
    }
}
