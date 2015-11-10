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
package de.qaware.chronix;


import de.qaware.chronix.converter.BinaryStorageDocument;
import de.qaware.chronix.converter.DocumentConverter;
import de.qaware.chronix.streaming.SolrStreamingService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The Chronix client to stream and add time series
 *
 * @param <T> the time series type
 * @author f.lautenschlager
 */
public class ChronixClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixClient.class);

    private static final int NR_OF_DOCUMENTS_PER_BATCH = 200;
    private static final int COMMIT_WITHIN_MS = 500;
    private final DocumentConverter<T> converter;


    /**
     * Constructs a Chronix client.
     *
     * @param converter - the document converter
     */
    public ChronixClient(DocumentConverter<T> converter) {
        this.converter = converter;
    }

    /**
     * Creates a stream of time series for the given query context and the connection
     *
     * @return a stream of time series
     */
    public Stream<T> stream(SolrQuery query, long queryStart, long queryEnd, SolrClient connection) {
        SolrStreamingService<T> solrStreamingService = new SolrStreamingService<>(converter, query, queryStart, queryEnd, connection, NR_OF_DOCUMENTS_PER_BATCH);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(solrStreamingService, Spliterator.SIZED), false);
    }

    /**
     * Add the given documents to the solr server connection.
     * Note that the solr server is responsible for the commit.
     *
     * @param timeSeries - the time series of type <T> that should stored
     * @param connection - the connection to the server
     * @return the server status of the add command.
     */
    public UpdateResponse add(Collection<T> timeSeries, SolrClient connection) {

        List<SolrInputDocument> collection = Collections.synchronizedList(new ArrayList<>());
        timeSeries.parallelStream().forEach(ts -> collection.add(convert(ts)));

        //adding a collection is faster than adding single documents
        try {
            return connection.add(collection, COMMIT_WITHIN_MS);
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Could not add document to solr.", e);
            return null;
        }

    }

    /**
     * Converts a time series of type <T> to SolInputDocument
     *
     * @param ts - the time series
     * @return a filled SolrInputDocument
     */
    private SolrInputDocument convert(T ts) {
        BinaryStorageDocument series = converter.to(ts);
        SolrInputDocument solrDocument = new SolrInputDocument();
        series.getFields().entrySet().forEach(entry -> solrDocument.addField(entry.getKey(), entry.getValue()));
        return solrDocument;
    }


}
