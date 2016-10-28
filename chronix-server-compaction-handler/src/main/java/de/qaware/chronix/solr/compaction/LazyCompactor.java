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
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;

import java.util.*;

import static java.util.Collections.emptySet;

/**
 * Takes documents and merges them to larger ones.
 *
 * @author alex.christ
 */
public class LazyCompactor {
    private int threshold;

    /**
     * Creates an instance.
     *
     * @param threshold the minimum number of data points to be merged into a single document.
     */
    public LazyCompactor(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Merges documents into larger ones
     *
     * @param documents the documents to compact
     * @param schema    the current solr schema
     * @return the compaction result
     */
    public Iterable<CompactionResult> compact(Iterable<Document> documents, IndexSchema schema) {
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
        public Iterator<CompactionResult> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return documents.hasNext();
        }

        @Override
        public CompactionResult next() {
            Set<Document> readDocs = new HashSet<>();
            List<MetricTimeSeries> readTs = new ArrayList<>();
            int numPoints = 0;
            while (documents.hasNext()) {
                if (numPoints >= threshold) {
                    return new CompactionResult(readDocs, doCompact(readTs));
                }
                Document document = documents.next();
                MetricTimeSeries ts = converterService.toTimeSeries(document, schema);
                readTs.add(ts);
                readDocs.add(document);
                numPoints += ts.size();
            }
            return new CompactionResult(readDocs, doCompact(readTs));
        }

        private Set<SolrInputDocument> doCompact(List<MetricTimeSeries> tsToCompact) {
            if (tsToCompact.isEmpty()) {
                return emptySet();
            }
            Iterator<MetricTimeSeries> it = tsToCompact.iterator();
            MetricTimeSeries first = it.next();
            it.forEachRemaining(ts -> first.addAll(ts.getTimestamps(), ts.getValues()));
            return ImmutableSet.of(converterService.toInputDocument(first));
        }
    }
}
