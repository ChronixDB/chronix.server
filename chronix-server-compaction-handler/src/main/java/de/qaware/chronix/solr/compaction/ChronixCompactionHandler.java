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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static de.qaware.chronix.Schema.START;
import static de.qaware.chronix.converter.common.MetricTSSchema.METRIC;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.lucene.search.SortField.Type.LONG;

/**
 * The Chronix compaction handler
 *
 * @author f.lautenschlager
 * @author alex.christ
 */
public class ChronixCompactionHandler extends RequestHandlerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixCompactionHandler.class);
    private static final String PARAM_METRICS = "metrics";
    private static final char PARAM_METRICS_SEPARATOR = ',';
    private final CompactionHandlerConfiguration config;

    /**
     * Creates a new instance. Constructor used by Solr.
     */
    public ChronixCompactionHandler() {
        config = new CompactionHandlerConfiguration();
    }

    /**
     * Creates a new instance. Constructor used by tests.
     *
     * @param config the configuration
     */
    public ChronixCompactionHandler(CompactionHandlerConfiguration config) {
        this.config = config;
    }

    @Override
    public String getDescription() {
        return "The Chronix compaction handler.";
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        String metrics = req.getParams().get(PARAM_METRICS);
        if (metrics == null) {
            LOGGER.error("No metrics names given");
            rsp.add("error", "No metrics names");
            return;
        }
        for (String metric : split(metrics, PARAM_METRICS_SEPARATOR)) {
            compact(req, rsp, metric);
        }
    }

    private void compact(SolrQueryRequest req, SolrQueryResponse rsp, String metric) throws IOException {
        SolrUpdateService updateService = config.getSolrUpdateService(req, rsp);
        SolrIndexSearcher searcher = req.getSearcher();
        IndexSchema schema = searcher.getSchema();
        Query query = new TermQuery(new Term(METRIC, metric));
        Sort sort = new Sort(new SortField(START, LONG));

        Iterable<Document> documents = config.getDocumentLoader().load(searcher, query, sort);
        Iterable<CompactionResult> compactionResults = config.getCompactor().compact(documents, schema);

        int compactedCount = 0;
        int resultCount = 0;
        for (CompactionResult compactionResult : compactionResults) {
            for (Document document : compactionResult.getInputDocuments()) {
                updateService.delete(document, req);
                compactedCount++;
            }
            for (SolrInputDocument document : compactionResult.getOutputDocuments()) {
                updateService.add(document, req);
                resultCount++;
            }
        }

        updateService.commit(req);

        rsp.add(metric + "-numCompacted", compactedCount);
        rsp.add(metric + "-numNewDocs", resultCount);
    }
}