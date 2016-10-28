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
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static de.qaware.chronix.Schema.START;
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
    private static final String JOIN_KEY = "joinKey";
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
        String joinKey = req.getParams().get(JOIN_KEY);
        if (joinKey == null) {
            LOGGER.error("No join key given.");
            rsp.add("error",
                    join("", "No join key given.",
                            " The join key is a comma separated list of fields and",
                            " represents the primary key of a time series."));
            return;
        }
        SolrFacetService facetService = config.solrFacetService(req, rsp);
        SolrUpdateService updateService = config.solrUpdateService(req, rsp);

        List<NamedList<Object>> pivotResult = facetService.pivot(joinKey, new MatchAllDocsQuery());
        List<TimeSeriesId> timeSeriesIds = facetService.toTimeSeriesIds(pivotResult);

        for (TimeSeriesId tsId : timeSeriesIds) {
            doCompact(req, rsp, tsId);
        }

        updateService.commit();
    }

    private void doCompact(SolrQueryRequest req,
                           SolrQueryResponse rsp,
                           TimeSeriesId tsId) throws IOException, SyntaxError {
        QParser parser = config.parser(req, tsId.toQuery());
        LazyCompactor compactor = config.compactor();
        LazyDocumentLoader documentLoader = config.documentLoader();
        SolrUpdateService updateService = config.solrUpdateService(req, rsp);
        SolrIndexSearcher searcher = req.getSearcher();
        Sort sort = new Sort(new SortField(START, LONG));

        Iterable<Document> documents = documentLoader.load(searcher, parser.getQuery(), sort);
        Iterable<CompactionResult> compactionResults = compactor.compact(documents, searcher.getSchema());

        int compactedCount = 0;
        int resultCount = 0;
        for (CompactionResult compactionResult : compactionResults) {
            for (Document document : compactionResult.getInputDocuments()) {
                updateService.delete(document);
                compactedCount++;
            }
            for (SolrInputDocument document : compactionResult.getOutputDocuments()) {
                updateService.add(document);
                resultCount++;
            }
        }

        rsp.add("timeseries " + tsId + " oldNumDocs:", compactedCount);
        rsp.add("timeseries " + tsId + " newNumDocs:", resultCount);
    }
}