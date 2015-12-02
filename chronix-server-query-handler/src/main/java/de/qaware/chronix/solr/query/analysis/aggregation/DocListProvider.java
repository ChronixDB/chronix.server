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
package de.qaware.chronix.solr.query.analysis.aggregation;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocList;

import java.io.IOException;

/**
 * Provider for better testing.
 *
 * @author f.lautenschlager
 */
public interface DocListProvider {
    /**
     * Returns a Solr DocList result
     *
     * @param q     - the user query
     * @param req   - the solr query request object
     * @param start - start of the query
     * @param limit - the document limit
     * @return DocList matching to the query
     * @throws IOException if there are problems with solr
     */
    DocList doSimpleQuery(String q, SolrQueryRequest req, int start, int limit) throws IOException;

}
