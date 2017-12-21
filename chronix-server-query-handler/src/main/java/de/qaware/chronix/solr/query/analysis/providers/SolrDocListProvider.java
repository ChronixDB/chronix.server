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
package de.qaware.chronix.solr.query.analysis.providers;

import de.qaware.chronix.solr.query.analysis.DocListProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.SolrPluginUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Solr DocList provider implementation.
 *
 * @author f.lautenschlager
 */
public class SolrDocListProvider implements DocListProvider {
    /**
     * Calls apache solr to answer the given user request.
     *
     * @param q     the user query
     * @param req   the solr query request object
     * @param start start of the query
     * @param limit the document limit
     * @return the result of the query
     * @throws IOException if bad things happen
     */
    @Override
    public DocList doSimpleQuery(String q, SolrQueryRequest req, int start, int limit) throws IOException {
        return SolrPluginUtils.doSimpleQuery(q, req, start, limit);
    }

    /**
     * Converts the docs into a solr document list.
     *
     * @param docs     The {@link org.apache.solr.search.DocList} to convert
     * @param searcher The {@link org.apache.solr.search.SolrIndexSearcher} to use to load the docs from the Lucene index
     * @param fields   The names of the Fields to load
     * @param ids      A map to store the ids of the docs
     * @return the docs as solr document list.
     * @throws IOException if bad things happen.
     */
    @Override
    public SolrDocumentList docListToSolrDocumentList(DocList docs, SolrIndexSearcher searcher, Set<String> fields, Map<SolrDocument, Integer> ids) throws IOException {
        return SolrPluginUtils.docListToSolrDocumentList(docs, searcher, fields, ids);
    }
}
