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
package de.qaware.chronix.solr;

import de.qaware.chronix.converter.DocumentConverter;
import de.qaware.chronix.solr.add.SolrAddingService;
import de.qaware.chronix.solr.stream.SolrStreamingService;
import de.qaware.chronix.streaming.StorageService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Apache Solr storage implementation of the Chronix StorageService interface
 *
 * @param <T> - the time series type
 */
public class ChronixSolrStorage<T> implements StorageService<T, SolrClient, SolrQuery> {

    @Override
    public Stream<T> stream(DocumentConverter<T> converter, SolrClient connection, SolrQuery query, long queryStart, long queryEnd, int nrOfDocumentsPerBatch) {
        SolrStreamingService<T> solrStreamingService = new SolrStreamingService<>(converter, query, queryStart, queryEnd, connection, nrOfDocumentsPerBatch);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(solrStreamingService, Spliterator.SIZED), false);
    }

    @Override
    public boolean add(DocumentConverter<T> converter, Collection<T> documents, SolrClient connection) {
        return SolrAddingService.add(converter, documents, connection);
    }


}
