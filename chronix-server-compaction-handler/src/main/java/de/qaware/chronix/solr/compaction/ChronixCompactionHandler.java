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
        int chunkSize = req.getParams().getInt(CHUNK_SIZE, 100000);
        int pageSize = req.getParams().getInt(PAGE_SIZE, 100);
        if (joinKey == null) {
            LOGGER.error("No join key given.");
            rsp.add("error",
                    join("", "No join key given.",
                            " The join key is a comma separated list of fields and",
                            " represents the primary key of a time series."));
            return;
        }

        SolrFacetService facetService = dependencyProvider.solrFacetService(req, rsp);
        List<NamedList<Object>> pivotResult = facetService.pivot(joinKey, new MatchAllDocsQuery());
        facetService.toTimeSeriesIds(pivotResult)
                .parallelStream()
                .forEach(timeSeriesId -> compact(req, rsp, timeSeriesId, chunkSize, pageSize));

        //commit final changes
        dependencyProvider.solrUpdateService(req, rsp).commit();
    }

    private void compact(SolrQueryRequest req, SolrQueryResponse rsp, TimeSeriesId tsId, int chunkSize, int pageSize) {
        try {
            doCompact(req, rsp, tsId, chunkSize, pageSize);
        } catch (IOException | SyntaxError e) {
            throw new IllegalStateException(e);
        }
    }

    private void doCompact(SolrQueryRequest req, SolrQueryResponse rsp,
                           TimeSeriesId tsId,
                           int chunkSize, int pageSize) throws IOException, SyntaxError {
        QParser parser = dependencyProvider.parser(req, tsId.toQuery());
        LazyCompactor compactor = dependencyProvider.compactor(chunkSize);
        LazyDocumentLoader documentLoader = dependencyProvider.documentLoader(pageSize);

        SolrIndexSearcher searcher = req.getSearcher();
        IndexSchema schema = searcher.getSchema();
        Query query = parser.getQuery();
        Sort sort = new Sort(new SortField(START, LONG));

        Iterable<Document> documents = documentLoader.load(searcher, query, sort);
        Iterable<CompactionResult> compactionResults = compactor.compact(documents, schema);

        int compactedCount = 0;
        int resultCount = 0;
        SolrUpdateService updateService = dependencyProvider.solrUpdateService(req, rsp);
        LinkedList<Document> docsToDelete = new LinkedList<>();

        for (CompactionResult compactionResult : compactionResults) {
            for (Document doc : compactionResult.getInputDocuments()) {
                docsToDelete.add(doc);
                compactedCount++;
            }
            for (SolrInputDocument doc : compactionResult.getOutputDocuments()) {
                updateService.add(doc);
                resultCount++;
            }
        }
        updateService.delete(docsToDelete);

        rsp.add("timeseries " + tsId + " oldNumDocs:", compactedCount);
        rsp.add("timeseries " + tsId + " newNumDocs:", resultCount);
    }


    /**
     * Provides dependencies and thereby
     */
    public class DependencyProvider {

        /**
         * @param req the request
         * @param rsp the response
         * @return the solr update service
         */
        public SolrUpdateService solrUpdateService(SolrQueryRequest req, SolrQueryResponse rsp) {
            return new SolrUpdateService(req, rsp);
        }

        /**
         * @param pageSize the page size
         * @return the document loader
         */
        public LazyDocumentLoader documentLoader(int pageSize) {
            return new LazyDocumentLoader(pageSize);
        }

        /**
         * @param chunkSize the chunkSize
         * @return the compactor
         */
        public LazyCompactor compactor(int chunkSize) {
            return new LazyCompactor(chunkSize);
        }

        /**
         * @return the solr facet service
         */
        public SolrFacetService solrFacetService(SolrQueryRequest req, SolrQueryResponse rsp) {
            return new SolrFacetService(req, rsp);
        }

        public QParser parser(SolrQueryRequest req, String query) throws SyntaxError {
            return QParser.getParser(query, req);
        }
    }
}