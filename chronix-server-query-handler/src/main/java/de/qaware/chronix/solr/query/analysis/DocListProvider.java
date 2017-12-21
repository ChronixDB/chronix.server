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
package de.qaware.chronix.solr.query.analysis;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Provider for better abstraction and testing.
 *
 * @author f.lautenschlager
 */
public interface DocListProvider {
    /**
     * Returns a Solr DocList result
     *
     * @param q     the user query
     * @param req   the solr query request object
     * @param start start of the query
     * @param limit the document limit
     * @return DocList matching to the query
     * @throws IOException if there are problems with solr
     */
    DocList doSimpleQuery(String q, SolrQueryRequest req, int start, int limit) throws IOException;

    /**
     * Convert a DocList to a SolrDocumentList
     * <p>
     * The optional param "ids" is populated with the lucene document id
     * for each SolrDocument.
     *
     * @param docs     The {@link org.apache.solr.search.DocList} to convert
     * @param searcher The {@link org.apache.solr.search.SolrIndexSearcher} to use to load the docs from the Lucene index
     * @param fields   The names of the Fields to load
     * @param ids      A map to store the ids of the docs
     * @return The new {@link org.apache.solr.common.SolrDocumentList} containing all the loaded docs
     * @throws java.io.IOException if there was a problem loading the docs
     * @since solr 1.4
     */
    SolrDocumentList docListToSolrDocumentList(DocList docs, SolrIndexSearcher searcher, Set<String> fields, Map<SolrDocument, Integer> ids) throws IOException;

}
