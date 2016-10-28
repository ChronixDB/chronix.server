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

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.PivotFacetProcessor;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocSet;

import static java.util.Collections.emptyList;

/**
 * Provides dependencies. Facilitates testing.
 *
 * @author alex.christ
 */
public class SolrComponentFactory {
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
