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
package de.qaware.chronix.solr.client.stream;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.qaware.chronix.converter.DocumentConverter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;

/**
 * The solr streaming service let one stream data from Solr.
 *
 * @param <T> type of the returned class
 * @author f.lautenschlager
 */
public class SolrStreamingService<T> implements Iterator<T> {

    /**
     * The class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrStreamingService.class);

    /**
     * The query and connection to solr
     */
    private final SolrQuery query;
    private final SolrClient connection;

    /**
     * Converter for converting the documents
     */
    private final DocumentConverter<T> converter;

    /**
     * Query parameters
     */
    private final long queryStart;
    private final long queryEnd;
    private int nrOfTimeSeriesPerBatch;
    private long nrOfAvailableTimeSeries;
    private int currentDocumentCount = 0;

    /**
     * The executor service to do the work asynchronously
     */
    private final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));

    /**
     * The handler for this service
     */
    private final SolrStreamingHandler solrStreamingHandler;


    /**
     * Handler waiting for future callbacks
     */
    private TimeSeriesHandler<T> timeSeriesHandler;

    /**
     * Constructs a streaming service
     *
     * @param converter              - the converter to convert documents
     * @param query                  - the solr query
     * @param connection             - the solr server connection
     * @param nrOfTimeSeriesPerBatch - the number of time series that are read by one query
     */
    public SolrStreamingService(DocumentConverter<T> converter, SolrQuery query, long queryStart, long queryEnd, SolrClient connection, int nrOfTimeSeriesPerBatch) {
        this.converter = converter;
        this.solrStreamingHandler = new SolrStreamingHandler();
        this.query = query;
        this.connection = connection;
        this.nrOfTimeSeriesPerBatch = nrOfTimeSeriesPerBatch;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
    }

    @Override
    public boolean hasNext() {
        //do a query to get the available documents
        nrOfAvailableTimeSeries = getNrOfDocuments(query, connection);
        if (nrOfAvailableTimeSeries <= 0) {
            return false;
        }
        //initialize the callback handler.
        //The queue size have to be the size of time series per batch
        if (timeSeriesHandler == null) {
            timeSeriesHandler = new TimeSeriesHandler<>(nrOfTimeSeriesPerBatch);
        }
        return currentDocumentCount < nrOfAvailableTimeSeries;
    }


    private long getNrOfDocuments(SolrQuery query, SolrClient connection) {
        SolrQuery solrQuery = query.getCopy();
        solrQuery.setRows(0);
        //we do not need any data from the server expect the total number of found documents
        solrQuery.setFields("");

        try {
            QueryResponse response = connection.query(solrQuery);
            return response.getResults().getNumFound();
        } catch (SolrServerException | IOException e) {
            LOGGER.error("SolrServerException occurred while querying server.", e);
        }
        return 0;
    }

    @Override
    public T next() {
        if (currentDocumentCount % nrOfTimeSeriesPerBatch == 0) {
            streamDocumentsFromSolr();
        }

        currentDocumentCount += 1;
        if (currentDocumentCount == nrOfAvailableTimeSeries) {
            LOGGER.debug("Shutting down the thread pool while all points are converted.");
            service.shutdown();
        }

        if (currentDocumentCount > nrOfAvailableTimeSeries) {
            throw new NoSuchElementException("Index " + currentDocumentCount + " greater than available time series " + nrOfAvailableTimeSeries);
        }

        return timeSeriesHandler.take();
    }

    private void streamDocumentsFromSolr() {
        Instant startTime = Instant.now();
        Instant timeLimit = startTime.plus(5, ChronoUnit.SECONDS);
        SolrQuery solrQuery = query.getCopy();
        solrQuery.setRows(nrOfTimeSeriesPerBatch);
        solrQuery.setStart(currentDocumentCount);

        solrStreamingHandler.init(nrOfTimeSeriesPerBatch, currentDocumentCount);

        try {
            connection.queryAndStreamResponse(solrQuery, solrStreamingHandler);
            SolrDocument document;
            do {
                document = solrStreamingHandler.pool();
                LOGGER.debug("Polling {} document", document);
                if (document != null) {
                    ListenableFuture future = service.submit(new DocumentConverterCaller<>(document, converter, queryStart, queryEnd));
                    Futures.addCallback(future, timeSeriesHandler);
                }

            } while (solrStreamingHandler.canPoll() || (document == null && startTime.isAfter(timeLimit)));
        } catch (SolrServerException | IOException e) {
            LOGGER.warn("Exception while streaming the data points from Solr", e);
        }
    }

}
