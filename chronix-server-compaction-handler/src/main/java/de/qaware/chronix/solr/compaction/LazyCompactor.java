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

import com.google.common.collect.ImmutableSet;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;

import java.util.Iterator;

/**
 * Takes documents and merges them to larger ones.
 * l
 *
 * @author alex.christ
 */
public class LazyCompactor {
    /**
     * Merges documents into larger ones
     *
     * @param documents the documents to compact
     * @param schema    the current solr schema
     * @return the compaction result
     */
    public Iterable<CompactionResult>   compact(Iterable<Document> documents, IndexSchema schema) {
        return new LazyCompactionResultSet(documents, schema);
    }

    private class LazyCompactionResultSet implements Iterator<CompactionResult>, Iterable<CompactionResult> {
        private final Iterator<Document> documents;
        private final ConverterService converterService;
        private final IndexSchema schema;

        private LazyCompactionResultSet(Iterable<Document> documents, IndexSchema schema) {
            this.documents = documents.iterator();
            this.schema = schema;
            converterService = new ConverterService();
        }

        @Override
        public boolean hasNext() {
            return documents.hasNext();
        }

        @Override
        public CompactionResult next() {
            Document document = documents.next();
            SolrDocument solrDoc = converterService.toSolrDoc(schema, document);
            MetricTimeSeries mts = converterService.toTimeSeries(solrDoc);
            SolrInputDocument inputDocument = converterService.toInputDocument(mts);
            return new CompactionResult(ImmutableSet.of(document), ImmutableSet.of(inputDocument));
        }

        @Override
        public Iterator<CompactionResult> iterator() {
            return this;
        }
    }
}
