/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.solr.stream;

import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.common.SolrDocument;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class to handle the callbacks from the solr streaming api
 *
 * @author f.lautenschlager
 */
public class SolrStreamingHandler extends StreamingResponseCallback {

    private long startIdx;
    private long nrOfTimeSeriesPerBatch;
    private Queue<SolrDocument> queue;
    private long currentPosition;
    private long numFound;

    private boolean readNextDocument;

    /**
     * And empty initialized handler.
     */
    public SolrStreamingHandler() {
        init(1, 0);
    }

    /**
     * Initializes the streaming handler.
     * e.g.
     * Total number of docs: 1000
     * nrOfTimeSeriesPerBatch = 200
     * startIdx = 1
     * => reads documents from 1 to 200
     *
     * @param nrOfTimeSeriesPerBatch - the number of time series read per batch
     * @param startIdx               - start of the query
     */
    public void init(int nrOfTimeSeriesPerBatch, int startIdx) {
        this.nrOfTimeSeriesPerBatch = nrOfTimeSeriesPerBatch;
        this.startIdx = startIdx;
        this.queue = new ArrayBlockingQueue<>(nrOfTimeSeriesPerBatch);
    }

    @Override
    public void streamDocListInfo(long documentsFoundForQuery, long documentIndexPosition, Float aMaxScore) {
        // called before start of streaming
        currentPosition = documentIndexPosition;
        numFound = documentsFoundForQuery;
        readNextDocument = numFound != 0;
    }


    @Override
    public void streamSolrDocument(SolrDocument requestedDocument) {
        currentPosition++;
        queue.add(requestedDocument);

        //we have hit the limit of total documents found for that query
        if (currentPosition == numFound) {
            readNextDocument = false;
            return;
        }

        //we have hit the batch limit
        if (currentPosition == (nrOfTimeSeriesPerBatch + startIdx)) {
            readNextDocument = false;
        }

    }

    /**
     * @return true if we do not have read all documents from solr or we have more documents in our queue, otherwise false
     */
    public boolean canPoll() {
        return readNextDocument || !queue.isEmpty();
    }

    /**
     * @return the document in the queue
     */
    public SolrDocument poll() {
        return queue.poll();
    }
}

