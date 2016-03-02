/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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
