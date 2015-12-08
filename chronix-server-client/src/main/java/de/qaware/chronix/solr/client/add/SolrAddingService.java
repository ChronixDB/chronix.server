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
package de.qaware.chronix.solr.client.add;

import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.TimeSeriesConverter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A service class to add time series to Apache Solr.
 **/
public class SolrAddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrAddingService.class);

    private SolrAddingService() {
        //Avoid instances
    }

    /**
     * Adds the given collection of time series to Apache Solr.
     * Note: The add method do not commit the time series
     *
     * @param converter  - the converter to converter the time series into a Solr document
     * @param timeSeries - the collection with time series
     * @param connection - the connection to the Apache solr.
     * @return true if successful, otherwise false
     */
    public static <T> boolean add(TimeSeriesConverter<T> converter, Collection<T> timeSeries, SolrClient connection) {

        if (timeSeries == null || timeSeries.isEmpty()) {
            LOGGER.debug("Collection is empty. Nothing to commit");
            return true;
        }

        List<SolrInputDocument> collection = Collections.synchronizedList(new ArrayList<>());
        timeSeries.parallelStream().forEach(ts -> collection.add(convert(ts, converter)));

        //adding a collection is faster than adding single documents
        try {
            return evaluate(connection.add(collection));
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Could not add document to solr.", e);
            return false;
        }
    }

    private static boolean evaluate(UpdateResponse response) {
        LOGGER.debug("Response returned: Status code {}, Elapsed time {}, QTime {}", response.getStatus(), response.getElapsedTime(), response.getQTime());
        //any other status code means 'there was an error'
        return response.getStatus() == 0;
    }

    /**
     * Converts a time series of type <T> to SolInputDocument
     *
     * @param ts - the time series
     * @return a filled SolrInputDocument
     */
    private static <T> SolrInputDocument convert(T ts, TimeSeriesConverter<T> converter) {
        BinaryTimeSeries series = converter.to(ts);
        SolrInputDocument solrDocument = new SolrInputDocument();
        series.getFields().entrySet().forEach(entry -> solrDocument.addField(entry.getKey(), entry.getValue()));
        return solrDocument;
    }
}
