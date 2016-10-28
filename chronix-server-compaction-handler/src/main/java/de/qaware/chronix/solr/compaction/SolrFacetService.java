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
package de.qaware.chronix.solr.compaction;

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.PivotFacetProcessor;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.solr.common.params.FacetParams.*;

/**
 * Executes solr facet queries.
 *
 * @author alex.christ
 */
public class SolrFacetService {

    private static final String FIELD_KEY = "field";
    private static final String VALUE_KEY = "value";
    private static final String PIVOT_KEY = "pivot";

    private final SolrQueryRequest req;
    private final SolrQueryResponse rsp;
    private final DependencyProvider dependencyProvider;

    /**
     * Creates a new instance.
     *
     * @param req the solr query request
     * @param rsp the solr query response
     */
    public SolrFacetService(SolrQueryRequest req, SolrQueryResponse rsp) {
        this.req = req;
        this.rsp = rsp;
        this.dependencyProvider = new DependencyProvider();
    }

    /**
     * Creates a new instance. Facilitates testing.
     *
     * @param req                the solr query request
     * @param rsp                the solr query response
     * @param dependencyProvider the dependency provider
     */
    public SolrFacetService(SolrQueryRequest req, SolrQueryResponse rsp, DependencyProvider dependencyProvider) {
        this.req = req;
        this.rsp = rsp;
        this.dependencyProvider = dependencyProvider;
    }

    /**
     * Calculates the pivot facets over documents matching the given filter query. Example output:
     * <p>
     * host: h01
     * pivot:
     * ..metric: cpu
     * ..metric: heap
     * ..pivot:
     * ....process: java
     * ....process: php
     * host: h02
     * ..metric: cpu
     * ..pivot:
     * ....process: java
     *
     * @param dimensions the comma separated list of fields
     * @param fq         the filter query
     * @return pivot table
     * @throws IOException iff something goes wrong
     */
    public List<NamedList<Object>> pivot(String dimensions, Query fq) throws IOException {
        ModifiableSolrParams params = new ModifiableSolrParams(req.getParams());
        params.set(FACET_PIVOT, split(dimensions, ','));
        params.set(FACET_ZEROS, false);
        params.set(FACET_LIMIT, -1);
        DocSet matchingDocs = req.getSearcher().getDocSet(fq);
        PivotFacetProcessor pivot = dependencyProvider.pivotFacetProcessor(req, rsp, matchingDocs, params);
        return pivot.process(new String[]{dimensions}).get(dimensions);
    }

    /**
     * TODO: this method shouldn't be here. I have yet to find a good place to put it in.
     * <p>
     * Converts the pivot table of {@link #pivot(String, Query)} into a list of time series IDs.
     * A concrete and specific time series is identified by a full path through all pivot dimensions. E.g.:
     * <p>
     * Input:
     * -----
     * host: h01
     * pivot:
     * ..metric: cpu
     * ..metric: heap
     * ..pivot:
     * ....process: java
     * ....process: php
     * host: h02
     * ..metric: cpu
     * ..pivot:
     * ....process: java
     * <p>
     * Output:
     * ------
     * [host: h01, metric: cpu]
     * [host: h01, metric: heap, process:java]
     * [host: h01, metric: heap, process:php]
     * [host: h02, metric: cpu, process:java]
     *
     * @param attributes pivot result
     * @return time series ids
     */
    public List<TimeSeriesId> toTimeSeriesIds(List<NamedList<Object>> attributes) {
        List<TimeSeriesId> result = new ArrayList<>();
        attributes.forEach(it -> toTimeSeriesIds(it, result, new HashMap<>()));
        return result;
    }

    private void toTimeSeriesIds(NamedList<Object> attributes, List<TimeSeriesId> output, Map<String, Object> tsId) {
        tsId.put((String) attributes.get(FIELD_KEY), attributes.get(VALUE_KEY));
        if (attributes.get(PIVOT_KEY) != null) {
            @SuppressWarnings("unchecked")
            List<NamedList<Object>> listOfAttributes = (List) attributes.get(PIVOT_KEY);
            listOfAttributes.forEach(it -> toTimeSeriesIds(it, output, new HashMap<>(tsId)));
        } else {
            output.add(new TimeSeriesId(tsId));
        }
    }

    /**
     * Provides dependencies and thereby facilitates testing.
     */
    public class DependencyProvider {
        /**
         * @param req          the solr query request
         * @param rsp          the solr query response
         * @param matchingDocs the set of matching documents
         * @param solrParams   the solr request params
         * @return pivot processor
         */
        PivotFacetProcessor pivotFacetProcessor(SolrQueryRequest req,
                                                SolrQueryResponse rsp,
                                                DocSet matchingDocs,
                                                SolrParams solrParams) {
            ResponseBuilder rb = new ResponseBuilder(req, rsp, emptyList());
            rb.doFacets = true;
            return new PivotFacetProcessor(req, matchingDocs, solrParams, rb);
        }
    }
}