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


import de.qaware.chronix.converter.BinaryStorageDocument;
import de.qaware.chronix.converter.DocumentConverter;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Converts the solr document into a binary storage document and calls the given document converter.
 *
 * @author f.lautenschlager
 */
public class DocumentConverterCaller<T> implements Callable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentConverterCaller.class);

    private final SolrDocument document;
    private final DocumentConverter<T> documentConverter;
    private final long queryEnd;
    private final long queryStart;

    /**
     * Constructs a SolrDocumentConverter.
     *
     * @param document          - the fields and values
     * @param documentConverter - the concrete document converter
     */
    public DocumentConverterCaller(final SolrDocument document, final DocumentConverter<T> documentConverter, long queryStart, long queryEnd) {
        this.document = document;
        this.documentConverter = documentConverter;
        this.queryStart = queryStart;
        this.queryEnd = queryEnd;
    }


    @Override
    public T call() throws Exception {
        BinaryStorageDocument.Builder timeSeriesBuilder = new BinaryStorageDocument.Builder();

        document.forEach(mapEntry -> addToBuilder(timeSeriesBuilder, mapEntry));
        LOGGER.debug("Calling document converter with {}", document);
        T timeSeries = documentConverter.from(timeSeriesBuilder.build(), queryStart, queryEnd);
        LOGGER.debug("Returning time series {} to callee", timeSeries);
        return timeSeries;
    }

    private void addToBuilder(BinaryStorageDocument.Builder timeSeriesBuilder, Map.Entry<String, Object> mapEntry) {
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
