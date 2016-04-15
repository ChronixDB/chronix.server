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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The date range query handler to convert date expression and
 * delegate the query to the default search handler
 *
 * @author f.lautenschlager
 */
public class ChronixQueryHandler extends RequestHandlerBase implements SolrCoreAware, PluginInfoInitialized {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixQueryHandler.class);

    private static final Set<String> REQUIRED_FIELDS = new HashSet<>();
    /**
     * The default solr search handler
     */
    private final SearchHandler searchHandler = new SearchHandler();

    /**
     * The analysis handler
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
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        LOGGER.debug("Handling request {}", req);
        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(req.getParams());

        String originQuery = modifiableSolrParams.get(CommonParams.Q);

        final long[] startAndEnd = dateRangeParser.getNumericQueryTerms(originQuery);
        final long queryStart = or(startAndEnd[0], -1L, 0L);
        final long queryEnd = or(startAndEnd[1], -1L, Long.MAX_VALUE);

        modifiableSolrParams.set(ChronixQueryParams.QUERY_START_LONG, String.valueOf(queryStart));
        modifiableSolrParams.set(ChronixQueryParams.QUERY_END_LONG, String.valueOf(queryEnd));
        String query = dateRangeParser.replaceRangeQueryTerms(originQuery);
        modifiableSolrParams.set(CommonParams.Q, query);

        //Set the min required fields if the user define a sub set of fields
        modifiableSolrParams.set(CommonParams.FL, requestedFields(modifiableSolrParams.get(CommonParams.FL), req.getSchema().getFields().keySet()));
        //Set the updated query
        req.setParams(modifiableSolrParams);

        //check the filter queries
        String[] filterQueries = modifiableSolrParams.getParams(CommonParams.FQ);

        //if we have an analysis or aggregation request
        if (contains(filterQueries, ChronixQueryParams.AGGREGATION_PARAM) || contains(filterQueries, ChronixQueryParams.ANALYSIS_PARAM)) {
            LOGGER.debug("Request is an analysis request.");
            analysisHandler.handleRequestBody(req, rsp);
        } else {
            //let the default search handler do its work
            LOGGER.debug("Request is a default request");
            searchHandler.handleRequestBody(req, rsp);
        }

        //add the converted start and end to the response
        rsp.getResponseHeader().add(ChronixQueryParams.QUERY_START_LONG, queryStart);
        rsp.getResponseHeader().add(ChronixQueryParams.QUERY_END_LONG, queryEnd);
    }

    private <T> T or(T value, T condition, T or) {
        if (value.equals(condition)) {
            return or;
        } else {
            return value;
        }
    }

    /**
     * Gets the requested fields.
     * Joins the REQUIRED_FIELDS_WITH_DATA and the user defined fields.
     * E.g.:
     * user requested fields: userField
     * => data,start,end,metric,userField
     *
     * @param fl     the solr fl param
     * @param schema the solr schema
     * @return the user defined fields and the required fields, or null if fl is null
     */
    private String requestedFields(String fl, Set<String> schema) {
        //As a result Solr will return everything
        if (fl == null || fl.isEmpty()) {
            return null;
        }

        //we do not have to remove fields
        if (fl.indexOf('-') == -1) {
            return fl + ChronixQueryParams.JOIN_SEPARATOR + String.join(ChronixQueryParams.JOIN_SEPARATOR, REQUIRED_FIELDS);
        } else {
            //the requested fields including -fields
            Set<String> fields = new HashSet<>(Arrays.asList(fl.split(ChronixQueryParams.JOIN_SEPARATOR)));
            //Check if we have only fields to remove
            if (onlyToRemove(fields)) {
                //
                Set<String> resultingFields = new HashSet<>(schema);
                //remove fields
                for (String field : fields) {
                    //one can remove the data field
                    resultingFields.remove(field.replace("-", "").trim());
                }
                return String.join(ChronixQueryParams.JOIN_SEPARATOR, resultingFields);
            }
        }
        return null;
    }

    private boolean onlyToRemove(Set<String> fields) {
        for (String field : fields) {
            if (!field.startsWith("-")) {
                return false;
            }
        }
        return true;
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
