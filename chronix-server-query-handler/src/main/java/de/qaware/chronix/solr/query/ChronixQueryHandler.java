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
package de.qaware.chronix.solr.query;

import com.google.common.base.Strings;
import de.qaware.chronix.Schema;
import de.qaware.chronix.cql.CQLJoinFunction;
import de.qaware.chronix.solr.query.analysis.AnalysisHandler;
import de.qaware.chronix.solr.query.analysis.providers.SolrDocListProvider;
import de.qaware.chronix.solr.query.date.DateQueryParser;
import org.apache.solr.common.StringUtils;
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

    static {
        REQUIRED_FIELDS.add(Schema.DATA);
        REQUIRED_FIELDS.add(Schema.START);
        REQUIRED_FIELDS.add(Schema.END);
        REQUIRED_FIELDS.add(Schema.NAME);
        REQUIRED_FIELDS.add(Schema.TYPE);
    }

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

    @Override
    public void init(PluginInfo info) {
        searchHandler.init(info);
        analysisHandler.init(info);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        LOGGER.debug("Handling request {}", req);
        final ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(req.getParams());

        final String originQuery = modifiableSolrParams.get(CommonParams.Q);

        final long[] startAndEnd = dateRangeParser.getNumericQueryTerms(originQuery);
        final long queryStart = or(startAndEnd[0], -1L, 0L);
        final long queryEnd = or(startAndEnd[1], -1L, Long.MAX_VALUE);

        modifiableSolrParams.set(ChronixQueryParams.QUERY_START_LONG, String.valueOf(queryStart));
        modifiableSolrParams.set(ChronixQueryParams.QUERY_END_LONG, String.valueOf(queryEnd));
        final String query = dateRangeParser.replaceRangeQueryTerms(originQuery);
        modifiableSolrParams.set(CommonParams.Q, query);

        //Set the min required fields if the user define a sub set of fields
        final String fields = modifiableSolrParams.get(CommonParams.FL);
        modifiableSolrParams.set(CommonParams.FL, requestedFields(fields, req.getSchema().getFields().keySet()));
        //Set the updated query
        req.setParams(modifiableSolrParams);

        //check the filter queries
        final String[] chronixFunctions = modifiableSolrParams.getParams(ChronixQueryParams.CHRONIX_FUNCTION);
        final String chronixJoin = modifiableSolrParams.get(ChronixQueryParams.CHRONIX_JOIN);


        //if we have an function query or someone wants the data as json or a join query
        if (arrayIsNotEmpty(chronixFunctions) || contains(ChronixQueryParams.DATA_AS_JSON, fields) || !StringUtils.isEmpty(chronixJoin)) {
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

    private boolean arrayIsNotEmpty(String[] array) {

        if (array == null) {
            return false;
        }

        for (String entry : array) {
            if (!Strings.isNullOrEmpty(entry)) {
                return true;
            }
        }
        return false;
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

        //the user wants a single additional field
        if (fl.indexOf('-') == -1 && fl.indexOf('+') == -1) {
            //hence we return the required fields and the requested fields
            return fl + CQLJoinFunction.JOIN_SEPARATOR + String.join(CQLJoinFunction.JOIN_SEPARATOR, REQUIRED_FIELDS);
        } else {
            //the user removes or adds a field to all fields
            Set<String> fields = new HashSet<>(Arrays.asList(fl.split(CQLJoinFunction.JOIN_SEPARATOR)));

            //Check if we have only fields to remove
            Set<String> resultingFields = new HashSet<>(schema);

            //If a user requests the data as json (fl=dataAsJson)
            if (fl.contains(ChronixQueryParams.DATA_AS_JSON)) {
                //if the field is dataAsJson -> add it to the fields.
                resultingFields.add(ChronixQueryParams.DATA_AS_JSON);
            }

            //remove fields that are marked with minus sign '-'
            for (String field : fields) {
                //we only remove the fields. We have already added all fields
                if (field.indexOf('-') > -1) {
                    //one can remove the data field
                    resultingFields.remove(field.substring(1));
                }

            }
            return String.join(CQLJoinFunction.JOIN_SEPARATOR, resultingFields);

        }
    }

    private boolean contains(String field, String fields) {
        return !(StringUtils.isEmpty(field) || StringUtils.isEmpty(fields)) && fields.contains(field);
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
