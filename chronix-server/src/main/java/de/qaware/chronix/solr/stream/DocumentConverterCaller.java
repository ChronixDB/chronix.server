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


import de.qaware.chronix.converter.BinaryStorageDocument;
import de.qaware.chronix.converter.DocumentConverter;
import org.apache.lucene.document.Field;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
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

        document.forEach(mapEntry -> {

            Object valueType = mapEntry.getValue();

            if (valueType instanceof Field) {
                //a single value field
                Field value = (Field) mapEntry.getValue();
                timeSeriesBuilder.field(mapEntry.getKey(), evaluateRawType(value));

            } else if (valueType instanceof Collection) {
                //the value is multi-value field
                Collection multiValueField = (Collection) mapEntry.getValue();
                timeSeriesBuilder.field(mapEntry.getKey(), evaluateMultiValueField(multiValueField));

            } else {
                LOGGER.warn("Field {} is not of type field or collection", mapEntry);
            }

        });
        return documentConverter.from(timeSeriesBuilder.build(), queryStart, queryEnd);
    }

    private Collection evaluateMultiValueField(Collection multiValue) {
        //Create a new instance to hold the raw values
        try {
            Collection<Object> rawDataCollection = multiValue.getClass().newInstance();

            multiValue.stream().filter(value -> value instanceof Field).forEach(value -> {
                Field fieldValue = (Field) value;
                rawDataCollection.add(evaluateRawType(fieldValue));
            });

            return rawDataCollection;

        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.info("Could not instance object of type {}", multiValue.getClass(), e);
        }

        return Collections.emptyList();
    }


    /**
     * Solr returns a stored field instead of the data value.
     * It do not provide a method that indicate the value type (e.g., long or string)
     * thus we have do it manually :-(
     *
     * @param value - stored field.
     * @return an object as primitive java value
     */
    private Object evaluateRawType(Field value) {

        if (value.binaryValue() != null) {
            return value.binaryValue().bytes;

        } else if (value.numericValue() != null) {
            return value.numericValue();

        } else if (value.stringValue() != null) {
            return value.stringValue();

        } else {
            LOGGER.warn("Could not determine type of field {}. Returning null as value", value);
            return null;
        }

    }
}
