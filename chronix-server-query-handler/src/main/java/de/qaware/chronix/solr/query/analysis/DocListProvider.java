/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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
