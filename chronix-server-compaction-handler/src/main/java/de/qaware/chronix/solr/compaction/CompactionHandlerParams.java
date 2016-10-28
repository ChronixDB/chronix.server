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
package de.qaware.chronix.solr.compaction;

/**
 * Defines URL parameters for the compaction handler.
 *
 * @author alex.christ
 */
public class CompactionHandlerParams {
    /**
     * Comma separated list of field names. Represents the primary key of a time series.
     * All documents with the key (i.e.: the same values for the given fields) will be compacted.
     */
    public static final String JOIN_KEY = "joinKey";

    /**
     * Minimum number of data points to be merged into a single document.
     * Default value: 100000.
     */
    public static final String THRESHOLD = "threshold";

    /**
     * Number of documents to be loaded into memory at a time.
     * Unofficial parameter for testing purposes only. May be deleted at any time.
     * Default value: 100
     */
    static final String PAGE_SIZE = "pageSize";

}