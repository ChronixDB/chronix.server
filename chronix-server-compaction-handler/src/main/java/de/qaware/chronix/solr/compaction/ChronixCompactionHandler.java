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
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static de.qaware.chronix.Schema.START;
import static de.qaware.chronix.solr.compaction.CompactionHandlerParams.*;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.lucene.search.SortField.Type.LONG;

/**
 * The Chronix compaction handler
 *
 * @author f.lautenschlager
 * @author alex.christ
 */
public class ChronixCompactionHandler extends RequestHandlerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixCompactionHandler.class);
    private final DependencyProvider depProvider;
    private static final Sort SORT = new Sort(new SortField(START, LONG));

    /**
     * Creates a new instance. Constructor used by Solr.
     */
    public ChronixCompactionHandler() {
        depProvider = new DependencyProvider();
    }

    /**
     * Creates a new instance. Constructor used by tests.
     *
     * @param depProvider the dependency provider
     */
    public ChronixCompactionHandler(DependencyProvider depProvider) {
        this.depProvider = depProvider;
    }

    @Override
    public String getDescription() {
        return "The Chronix compaction handler.";
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        String joinKey = req.getParams().get(JOIN_KEY);
        String fq = req.getParams().get(FQ);
        int ppc = req.getParams().getInt(POINTS_PER_CHUNK, 100000);
        int pageSize = req.getParams().getInt(PAGE_SIZE, 100);

        depProvider.init(req, rsp);

        LazyCompactor compactor = depProvider.compactor(ppc, req.getSearcher().getSchema());
        LazyDocumentLoader documentLoader = depProvider.documentLoader(pageSize, req.getSearcher());

        if (isBlank(joinKey) && isBlank(fq)) {
            LOGGER.error("Neither join key nor filter query given.");
            rsp.add("error",
                    join("", "Neither join key nor filter query given.",
                            "Get help at https://chronix.gitbooks.io/chronix/content/document_compaction.html."));
            return;
        }

        //no join key => compact documents matching fq
        if (isBlank(joinKey)) {
            compact(documentLoader, compactor, rsp, fq, fq);
            depProvider.solrUpdateService().commit();
            return;
        }

        //determine time series identified by joinKey
        SolrFacetService facetService = depProvider.solrFacetService();
        Query filterQuery = isBlank(fq) ? new MatchAllDocsQuery() : depProvider.parser(fq).getQuery();
        List<NamedList<Object>> pivotResult = facetService.pivot(joinKey, filterQuery);

        //compact each time series' constituting documents
        facetService.toTimeSeriesIds(pivotResult)
                .parallelStream()
                .forEach(tsId -> compact(documentLoader, compactor, rsp, tsId.toString(), and(tsId.toQuery(), fq)));

        depProvider.solrUpdateService().commit();
    }

    private void compact(LazyDocumentLoader loader, LazyCompactor compactor, SolrQueryResponse rsp, String tsId, String q) {
        try {
            doCompact(loader, compactor, rsp, tsId, q);
        } catch (IOException | SyntaxError e) {
            // throw unchecked in order to call method from lambda expressions
            throw new IllegalStateException(e);
        }
    }

    private void doCompact(LazyDocumentLoader documentLoader,
                           LazyCompactor compactor,
                           SolrQueryResponse rsp,
                           String tsId,
                           String q) throws IOException, SyntaxError {
        Query query = depProvider.parser(q).getQuery();

        Iterable<Document> docs = documentLoader.load(query, SORT);
        Iterable<CompactionResult> compactionResults = compactor.compact(docs);

        List<Document> docsToDelete = new LinkedList<>();
        List<SolrInputDocument> docsToAdd = new LinkedList<>();

        compactionResults.forEach(it -> {
            docsToDelete.addAll(it.getInputDocuments());
            docsToAdd.addAll(it.getOutputDocuments());
        });

        depProvider.solrUpdateService().add(docsToAdd);
        depProvider.solrUpdateService().delete(docsToDelete);

        rsp.add("timeseries " + tsId + " oldNumDocs:", docsToDelete.size());
        rsp.add("timeseries " + tsId + " newNumDocs:", docsToAdd.size());
    }

    /**
     * Provides dependencies and thereby facilitates testing.
     */
    public static class DependencyProvider {
        private SolrQueryRequest req;
        private SolrUpdateService updateService;
        private SolrFacetService facetService;

        /**
         * Initializes instance. Must be called before calling any other methods on this instance.
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
         * @param searcher the searcher
         * @return the document loader
         */
        public LazyDocumentLoader documentLoader(int pageSize, IndexSearcher searcher) {
            return new LazyDocumentLoader(pageSize, searcher);
        }

        /**
         * @param pointsPerChunk the pointsPerChunk
         * @param schema         the schema
         * @return the compactor
         */
        public LazyCompactor compactor(int pointsPerChunk, IndexSchema schema) {
            return new LazyCompactor(pointsPerChunk, schema);
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

    private String and(String... clauses) {
        return stream(clauses).filter(Objects::nonNull).map(it -> join("", "(", it, ")")).collect(joining(" AND "));
    }
}