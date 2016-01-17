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
package de.qaware.chronix.solr.client;

import de.qaware.chronix.converter.TimeSeriesConverter;
import de.qaware.chronix.solr.client.add.SolrAddingService;
import de.qaware.chronix.solr.client.stream.SolrStreamingService;
import de.qaware.chronix.streaming.StorageService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

/**
 * Apache Solr storage implementation of the Chronix StorageService interface
 *
 * @param <T> - the time series type
 */
public class ChronixSolrStorage<T> implements StorageService<T, SolrClient, SolrQuery> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronixSolrStorage.class);

    private final int nrOfDocumentPerBatch;
    private final BinaryOperator<T> reduce;
    private final Function<T, String> groupBy;

    /**
     * Constructs a Chronix storage that is based on Apache solr.
     *
     * @param nrOfDocumentPerBatch number of documents that are processed in one batch
     * @param groupBy              the function to group time series records
     * @param reduce               the function to reduce the grouped time series records into one time series
     */
    public ChronixSolrStorage(final int nrOfDocumentPerBatch, final Function<T, String> groupBy, final BinaryOperator<T> reduce) {
        this.nrOfDocumentPerBatch = nrOfDocumentPerBatch;
        this.groupBy = groupBy;
        this.reduce = reduce;
    }

    /**
     * Queries apache solr and returns the time series in a stream.
     *
     * @param converter  the time series converter
     * @param connection the connection to apache solr
     * @param query      the user query
     * @return a stream of time series
     */
    @Override
    public Stream<T> stream(TimeSeriesConverter<T> converter, SolrClient connection, SolrQuery query) {
        LOGGER.debug("Streaming data from solr using converter {}, Solr Client {}, and Solr Query {}", converter, connection, query);
        SolrStreamingService<T> solrStreamingService = new SolrStreamingService<>(converter, query, connection, nrOfDocumentPerBatch);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(solrStreamingService, Spliterator.SIZED), false)
                .collect(groupingBy((Function<T, String>) groupBy::apply)).values().stream()
                .map(ts -> ts.stream().reduce(reduce).get());


    }

    /**
     * Adds the given collection of documents to the solr connection using the collector.
     * Note: The function does not call commit on the connection. Documents are just added to apache solr.
     *
     * @param converter  the converter matching the type <T>
     * @param documents  the documents of type <T>
     * @param connection the connection to apache solr
     * @return true if the documents are added to apache solr.
     */
    @Override
    public boolean add(TimeSeriesConverter<T> converter, Collection<T> documents, SolrClient connection) {
        return SolrAddingService.add(converter, documents, connection);
    }


}
