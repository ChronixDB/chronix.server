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
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import java.io.IOException;

import static de.qaware.chronix.Schema.ID;

/**
 * Executes update actions on solr.
 *
 * @author alex.christ
 */
public class SolrUpdateService {

    private UpdateRequestProcessor updateProcessor;
    private SolrQueryRequest req;

    /**
     * Creates a new instance.
     *
     * @param req the solr request
     * @param rsp the solr response
     */
    public SolrUpdateService(SolrQueryRequest req, SolrQueryResponse rsp) {
        this(req, req.getCore()
                .getUpdateProcessorChain(req.getParams())
                .createProcessor(req, rsp));
    }

    /**
     * Creates a new instance, mainly for testing purposes
     *
     * @param req             the solr query request
     * @param updateProcessor the update processor
     */
    SolrUpdateService(SolrQueryRequest req, UpdateRequestProcessor updateProcessor) {
        this.updateProcessor = updateProcessor;
        this.req = req;
    }

    /**
     * Deletes the given document from the solr index without commit.
     *
     * @param doc the document to delete
     * @throws IOException iff something goes wrong
     */
    public void delete(Document doc) throws IOException {
        DeleteUpdateCommand deleteUpdateCommand = new DeleteUpdateCommand(req);
        deleteUpdateCommand.setQuery(ID + ":" + doc.get(ID));
        updateProcessor.processDelete(deleteUpdateCommand);
    }

    /**
     * Adds the given document to the solr index without commit.
     *
     * @param doc the document to add
     * @throws IOException iff something goes wrong
     */
    public void add(SolrInputDocument doc) throws IOException {
        AddUpdateCommand cmd = new AddUpdateCommand(req);
        cmd.solrDoc = doc;
        updateProcessor.processAdd(cmd);
    }

    /**
     * Commits open changes to the solr index. Does not optimize the index afterwards.
     *
     * @throws IOException iff something goes wrong
     */
    public void commit() throws IOException {
        updateProcessor.processCommit(new CommitUpdateCommand(req, false));
    }
}
