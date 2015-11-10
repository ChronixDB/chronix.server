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
package de.qaware.chronix.converter;

/**
 * The document converter interface.
 * <p>
 * Created by f.lautenschlager on 12.01.2015.
 */
public interface DocumentConverter<T> {

    /**
     * Shall create an object of type T from the given time series document.
     * <p>
     * The time series document contains all stored fields in the Solr index.
     * This method is executed in worker thread and should handle the complete transformation into
     * a user custom time series object.
     *
     * @param binaryStorageDocument - the time series document containing all stored fields and values
     * @param queryStart            - the start of the query
     * @param queryEnd              - the end of the query
     * @return a concrete object of type T
     */
    T from(BinaryStorageDocument binaryStorageDocument, long queryStart, long queryEnd);

    /**
     * Shall do the conversation of the custom T into the TimeSeriesDocument that is stored.
     * The values should be in a string or a numerical representation.
     * <code>
     * put("FieldName","String-Value")
     * put("FieldName",478)
     * </code>
     *
     * @param document - the custom time series with all fields
     * @return the time series document that is stored
     */
    BinaryStorageDocument to(T document);

}
