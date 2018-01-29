/*
 * Copyright (C) 2018 QAware GmbH
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
package de.qaware.chronix.server.types;

import de.qaware.chronix.server.functions.ChronixFunction;
import org.apache.solr.common.SolrDocument;

import java.util.List;

/**
 * The interface defines a Chronix type.
 *
 * @author f.lautenschlager
 */
public interface ChronixType<T> {

    /**
     * @return the type name. Must be unique within an index.
     */
    String getType();

    /**
     * Converts the given list of records to specific type of time series
     *
     * @param joinKey    the join key that defines the group criteria
     * @param records    a list of records that belong to the query
     * @param queryStart the start of the query, use it to filter the records
     * @param queryEnd   the end of the query, use it fo filter the records
     * @return a time series of type <t>
     */
    ChronixTimeSeries<T> convert(String joinKey, List<SolrDocument> records, long queryStart, long queryEnd, boolean rawDataIsRequested);

    /**
     * @param function the query name of the function
     * @return the matching function
     */
    ChronixFunction<T> getFunction(String function);
}
