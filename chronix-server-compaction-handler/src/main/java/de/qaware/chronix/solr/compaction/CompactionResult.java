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
package de.qaware.chronix.solr.compaction;

import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrInputDocument;

import java.util.Set;

/**
 * Result of a compaction.
 * Contains the "input" documents, i.e. the documents that were compacted.
 * Contains the "output" documents, i.e. the documents containing the compacted data.
 *
 * @author alex.christ
 */
public class CompactionResult {
    private Set<Document> inputDocuments;
    private Set<SolrInputDocument> outputDocuments;

    /**
     * Creates a new instance
     *
     * @param inputDocuments  the original documents
     * @param outputDocuments the resulting documents
     */
    public CompactionResult(Set<Document> inputDocuments, Set<SolrInputDocument> outputDocuments) {
        this.inputDocuments = inputDocuments;
        this.outputDocuments = outputDocuments;
    }

    public Set<Document> getInputDocuments() {
        return inputDocuments;
    }

    public Set<SolrInputDocument> getOutputDocuments() {
        return outputDocuments;
    }
}
