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
package de.qaware.chronix.solr.client.stream;


import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.TimeSeriesConverter;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Converts the solr document into a binary time series and calls the given document converter.
 *
 * @param <T> the type of the returned time series class
 * @author f.lautenschlager
 */
public class TimeSeriesConverterCaller<T> implements Callable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesConverterCaller.class);

    private final SolrDocument document;
    private final TimeSeriesConverter<T> documentConverter;
    private final long queryEnd;
    private final long queryStart;

    /**
     * Constructs a SolrDocumentConverter.
     *
     * @param document          - the fields and values
     * @param documentConverter - the concrete document converter
     */
    public TimeSeriesConverterCaller(final SolrDocument document, final TimeSeriesConverter<T> documentConverter, long queryStart, long queryEnd) {
        this.document = document;
        this.documentConverter = documentConverter;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
    }


    @Override
    @SuppressWarnings("pmd:SignatureDeclareThrowsException")
    public T call() throws Exception {
        BinaryTimeSeries.Builder timeSeriesBuilder = new BinaryTimeSeries.Builder();

        document.forEach(mapEntry -> addToBuilder(timeSeriesBuilder, mapEntry));
        LOGGER.debug("Calling document converter with {}", document);
        T timeSeries = documentConverter.from(timeSeriesBuilder.build(), queryStart, queryEnd);
        LOGGER.debug("Returning time series {} to callee", timeSeries);
        return timeSeries;
    }

    private void addToBuilder(BinaryTimeSeries.Builder timeSeriesBuilder, Map.Entry<String, Object> mapEntry) {
        Object valueType = mapEntry.getValue();

        //If we use the default remote solr server, we get pojo's
        if (isPOJO(valueType)) {
            timeSeriesBuilder.field(mapEntry.getKey(), mapEntry.getValue());
        } else {
            LOGGER.warn("Field {} is not of type field or collection", mapEntry);
        }
    }

    private boolean isPOJO(Object valueType) {
        return valueType instanceof byte[] || valueType instanceof String || valueType instanceof Number || valueType instanceof Collection;
    }
}
