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

import org.apache.solr.search.SyntaxError;

import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Set of field-name and field-value pairs identifying a time series.
 *
 * @author alex.christ
 */
public class TimeSeriesId {
    private Map<String, Object> id;

    /**
     * Creates a new instance.
     *
     * @param attributes the identifying attributes
     */
    public TimeSeriesId(Map<String, Object> attributes) {
        this.id = attributes;
    }

    /**
     * Creates a lucene query macthing all documents belonging to this time series.
     *
     * @return query
     * @throws SyntaxError iff something goes wrong
     */
    public String toQuery() throws SyntaxError {
        return id.entrySet().stream().map(it -> it.getKey() + ":" + it.getValue()).collect(joining(" AND "));
    }

    @Override
    public String toString() {
        return "[" + id.entrySet().stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .map(it -> it.getKey() + ":" + it.getValue())
                .collect(joining(",")) + "]";
    }
}