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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static de.qaware.chronix.Schema.START;
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.*;
import static java.lang.String.join;
import static org.apache.lucene.search.SortField.Type.LONG;

/**
 * The Chronix compaction handler
 *
 * @author f.lautenschlager
 * @author alex.christ
 */
public class ChronixCompactionHandler extends RequestHandlerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixCompactionHandler.class);
    private final DependencyProvider dependencyProvider;
    private static final Sort SORT = new Sort(new SortField(START, LONG));

    /**
     * Creates a new instance. Constructor used by Solr.
     */
    public ChronixCompactionHandler() {
        dependencyProvider = new DependencyProvider();
    }

    /**
     * Creates a new instance. Constructor used by tests.
     *
     * @param dependencyProvider the dependency provider
     */
    public ChronixCompactionHandler(DependencyProvider dependencyProvider) {
        this.dependencyProvider = dependencyProvider;
    }

    @Override
    public String getDescription() {
        return "The Chronix compaction handler.";
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        String joinKey = req.getParams().get(JOIN_KEY);
        int ppc = req.getParams().getInt(POINTS_PER_CHUNK, 100000);
        int pageSize = req.getParams().getInt(PAGE_SIZE, 100);
        if (joinKey == null) {
            LOGGER.error("No join key given.");
            rsp.add("error",
                    join("", "No join key given.",
                            " The join key is a comma separated list of fields and",
                            " represents the primary key of a time series."));
            return;
        }

        dependencyProvider.init(req, rsp);
        SolrFacetService facetService = dependencyProvider.solrFacetService();
        List<NamedList<Object>> pivotResult = facetService.pivot(joinKey, new MatchAllDocsQuery());

        facetService.toTimeSeriesIds(pivotResult)
                .parallelStream()
                .forEach(timeSeriesId -> compact(req.getSearcher(), rsp, timeSeriesId, ppc, pageSize));

        dependencyProvider.solrUpdateService().commit();
    }

    private void compact(SolrIndexSearcher searcher, SolrQueryResponse rsp, TimeSeriesId tsId, int ppc, int pageSize) {
        try {
            // throw unchecked exception in order to call this method in a lambda expression
            doCompact(searcher, rsp, tsId, ppc, pageSize);
        } catch (IOException | SyntaxError e) {
            throw new IllegalStateException(e);
        }
    }

    private void doCompact(SolrIndexSearcher searcher,
                           SolrQueryResponse rsp,
                           TimeSeriesId tsId,
                           int ppc,
                           int pageSize) throws IOException, SyntaxError {
        Query query = dependencyProvider.parser(tsId.toQuery()).getQuery();
        IndexSchema schema = searcher.getSchema();

        int compactedCount = 0;
        int resultCount = 0;

        Iterable<Document> docs = dependencyProvider.documentLoader(pageSize).load(searcher, query, SORT);
        Iterable<CompactionResult> compactionResults = dependencyProvider.compactor(ppc).compact(docs, schema);
        List<Document> docsToDelete = new LinkedList<>();

        for (CompactionResult compactionResult : compactionResults) {
            for (Document doc : compactionResult.getInputDocuments()) {
                docsToDelete.add(doc);
                compactedCount++;
            }
            for (SolrInputDocument doc : compactionResult.getOutputDocuments()) {
                dependencyProvider.solrUpdateService().add(doc);
                resultCount++;
            }
        }
        dependencyProvider.solrUpdateService().delete(docsToDelete);

        rsp.add("timeseries " + tsId + " oldNumDocs:", compactedCount);
        rsp.add("timeseries " + tsId + " newNumDocs:", resultCount);
    }

    /**
     * Provides dependencies and thereby facilitates testing.
     */
    public class DependencyProvider {
        private SolrQueryRequest req;
        private SolrUpdateService updateService;
        private SolrFacetService facetService;

        /**
         * Initializes
         *
         * @param req the solr query request
         * @param rsp the solr query response
         */
        public void init(SolrQueryRequest req, SolrQueryResponse rsp) {
            this.req = req;
            this.updateService = new SolrUpdateService(req, rsp);
            this.facetService = new SolrFacetService(req, rsp);

        }

        /**
         * @return the solr update service
         */
        public SolrUpdateService solrUpdateService() {
            return updateService;
        }

        /**
         * @param pageSize the page size
         * @return the document loader
         */
        public LazyDocumentLoader documentLoader(int pageSize) {
            return new LazyDocumentLoader(pageSize);
        }

        /**
         * @param pointsPerChunk the pointsPerChunk
         * @return the compactor
         */
        public LazyCompactor compactor(int pointsPerChunk) {
            return new LazyCompactor(pointsPerChunk);
        }

        /**
         * @return the solr facet service
         */
        public SolrFacetService solrFacetService() {
            return facetService;
        }

        /**
         * @param query the query
         * @return a lucene qparser
         * @throws SyntaxError iff something goes wrong
         */
        public QParser parser(String query) throws SyntaxError {
            return QParser.getParser(query, req);
        }
    }
}